import logging
from pathlib import Path

import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler

from config import settings
from utils.model_persistence import save_model, load_model

logger = logging.getLogger(__name__)

_FEATURES = ["power_kw", "voltage_v", "current_a", "temperature_c", "power_factor"]


class IsolationForestDetector:
    def __init__(self, device_id: str):
        self.device_id = device_id
        self.model: IsolationForest | None = None
        self.scaler: StandardScaler | None = None
        self._model_key = f"isolation_forest_{device_id}"

    # ------------------------------------------------------------------
    def _extract_features(self, df: pd.DataFrame) -> np.ndarray:
        available = [c for c in _FEATURES if c in df.columns]
        X = df[available].copy()
        X = X.ffill().fillna(0)
        return X.values, available

    # ------------------------------------------------------------------
    def train(self, df: pd.DataFrame) -> None:
        X, used = self._extract_features(df)
        self.scaler = StandardScaler()
        X_scaled = self.scaler.fit_transform(X)
        self.model = IsolationForest(
            contamination=settings.ANOMALY_CONTAMINATION,
            n_estimators=200,
            random_state=42,
            n_jobs=-1,
        )
        self.model.fit(X_scaled)
        save_model({"model": self.model, "scaler": self.scaler, "features": used}, self._model_key)
        logger.info("IsolationForest trained for device %s on %d samples", self.device_id, len(X))

    # ------------------------------------------------------------------
    def predict(self, df: pd.DataFrame) -> list[dict]:
        artifact = load_model(self._model_key)
        if artifact is None:
            logger.warning("No saved model for %s — training on-the-fly", self.device_id)
            self.train(df)
            artifact = {"model": self.model, "scaler": self.scaler, "features": _FEATURES}

        model: IsolationForest = artifact["model"]
        scaler: StandardScaler = artifact["scaler"]
        used_features: list[str] = artifact["features"]

        available = [c for c in used_features if c in df.columns]
        X = df[available].ffill().fillna(0).values
        X_scaled = scaler.transform(X)

        scores = model.score_samples(X_scaled)          # negative; lower = more anomalous
        predictions = model.predict(X_scaled)           # -1 anomaly, 1 normal

        results = []
        for i, row in df.iterrows():
            score = float(scores[i if isinstance(i, int) else df.index.get_loc(i)])
            is_anomaly = predictions[i if isinstance(i, int) else df.index.get_loc(i)] == -1
            # Normalise score to [0, 1] confidence
            confidence = float(np.clip((score - settings.ANOMALY_SCORE_THRESHOLD) /
                                       abs(settings.ANOMALY_SCORE_THRESHOLD), 0, 1))
            results.append({
                "timestamp": row.get("timestamp"),
                "device_id": self.device_id,
                "anomaly_score": score,
                "is_anomaly": bool(is_anomaly),
                "confidence": confidence if is_anomaly else 1 - confidence,
                "features_used": available,
            })
        return results

import logging

import numpy as np
import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import StandardScaler

from config import settings
from utils.model_persistence import save_model, load_model

logger = logging.getLogger(__name__)

_FEATURES = [
    "runtime_hours", "power_kw", "voltage_v", "current_a",
    "temperature_c", "vibration_ms2", "last_maintenance_days",
]

_RISK_LABELS = {
    (0.0, 0.3): ("LOW", "No immediate action required. Continue routine monitoring."),
    (0.3, 0.5): ("MEDIUM", "Schedule inspection within 30 days."),
    (0.5, 0.7): ("HIGH", "Schedule maintenance within 7 days."),
    (0.7, 1.01): ("CRITICAL", "Immediate maintenance required. Risk of imminent failure."),
}


def _risk_label(prob: float) -> tuple[str, str]:
    for (lo, hi), (level, action) in _RISK_LABELS.items():
        if lo <= prob < hi:
            return level, action
    return "CRITICAL", "Immediate maintenance required."


class MaintenanceRandomForest:
    def __init__(self, device_id: str):
        self.device_id = device_id
        self._model_key = f"maintenance_rf_{device_id}"

    def _extract(self, df: pd.DataFrame) -> tuple[np.ndarray, list[str]]:
        available = [c for c in _FEATURES if c in df.columns]
        X = df[available].ffill().fillna(0).values
        return X, available

    def train(self, df: pd.DataFrame, labels: np.ndarray) -> None:
        X, used = self._extract(df)
        scaler = StandardScaler()
        X_scaled = scaler.fit_transform(X)

        model = RandomForestClassifier(
            n_estimators=settings.RF_N_ESTIMATORS,
            max_depth=settings.RF_MAX_DEPTH,
            class_weight="balanced",
            random_state=42,
            n_jobs=-1,
        )
        model.fit(X_scaled, labels)
        save_model({"model": model, "scaler": scaler, "features": used}, self._model_key)
        logger.info("Maintenance RF trained for device %s", self.device_id)

    def predict(self, df: pd.DataFrame) -> dict:
        artifact = load_model(self._model_key)

        if artifact is None:
            # Cold-start: derive a heuristic failure probability from temperature and runtime
            logger.warning("No RF model for %s — using heuristic", self.device_id)
            last = df.iloc[-1]
            prob = min(1.0, float(
                0.3 * (last.get("temperature_c", 50) / 100)
                + 0.3 * (last.get("runtime_hours", 0) / 10000)
                + 0.4 * (last.get("last_maintenance_days", 0) / 365)
            ))
            risk, action = _risk_label(prob)
            return {
                "device_id": self.device_id,
                "failure_probability": prob,
                "predicted_rul_days": max(0.0, round((1 - prob) * 365, 1)),
                "risk_level": risk,
                "recommended_action": action,
                "top_risk_factors": [],
            }

        model: RandomForestClassifier = artifact["model"]
        scaler: StandardScaler = artifact["scaler"]
        used_features: list[str] = artifact["features"]

        available = [c for c in used_features if c in df.columns]
        X = df[available].ffill().fillna(0).values
        X_scaled = scaler.transform(X)

        proba = model.predict_proba(X_scaled)[:, 1]
        prob = float(np.mean(proba))
        risk, action = _risk_label(prob)

        importances = model.feature_importances_
        top_idx = np.argsort(importances)[::-1][:3]
        top_factors = [available[i] for i in top_idx]

        rul = max(0.0, round((1 - prob) * 365, 1))
        return {
            "device_id": self.device_id,
            "failure_probability": prob,
            "predicted_rul_days": rul,
            "risk_level": risk,
            "recommended_action": action,
            "top_risk_factors": top_factors,
        }

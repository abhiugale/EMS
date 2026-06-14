import logging
from typing import Any

import numpy as np
import pandas as pd

from config import settings
from utils.model_persistence import save_model, load_model

logger = logging.getLogger(__name__)

_FEATURES = ["power_kw", "voltage_v", "current_a", "temperature_c"]


def _build_autoencoder(seq_len: int, n_features: int, latent_dim: int):
    """Build a Keras LSTM autoencoder. Import TF lazily to keep startup fast."""
    import tensorflow as tf
    from tensorflow import keras

    inputs = keras.Input(shape=(seq_len, n_features))
    # Encoder
    encoded = keras.layers.LSTM(latent_dim, return_sequences=False)(inputs)
    # Bottleneck
    bottleneck = keras.layers.RepeatVector(seq_len)(encoded)
    # Decoder
    decoded = keras.layers.LSTM(latent_dim, return_sequences=True)(bottleneck)
    outputs = keras.layers.TimeDistributed(keras.layers.Dense(n_features))(decoded)

    model = keras.Model(inputs, outputs, name="lstm_autoencoder")
    model.compile(optimizer="adam", loss="mse")
    return model


def _make_sequences(X: np.ndarray, seq_len: int) -> np.ndarray:
    return np.array([X[i : i + seq_len] for i in range(len(X) - seq_len + 1)])


class LSTMAutoencoder:
    def __init__(self, device_id: str):
        self.device_id = device_id
        self.model: Any | None = None
        self.threshold: float = 0.0
        self._model_key = f"lstm_autoencoder_{device_id}"
        self._seq_len = settings.LSTM_SEQUENCE_LENGTH

    def _prepare(self, df: pd.DataFrame) -> tuple[np.ndarray, list[str]]:
        from sklearn.preprocessing import MinMaxScaler
        available = [c for c in _FEATURES if c in df.columns]
        X = df[available].ffill().fillna(0).values.astype(np.float32)
        scaler = getattr(self, "_scaler", None)
        if scaler is None:
            from sklearn.preprocessing import MinMaxScaler
            self._scaler = MinMaxScaler()
            X = self._scaler.fit_transform(X)
        else:
            X = scaler.transform(X)
        return X, available

    def train(self, df: pd.DataFrame) -> None:
        X, used = self._prepare(df)
        seqs = _make_sequences(X, self._seq_len)
        n_features = X.shape[1]

        self.model = _build_autoencoder(self._seq_len, n_features, settings.LSTM_LATENT_DIM)
        self.model.fit(
            seqs, seqs,
            epochs=settings.LSTM_EPOCHS,
            batch_size=settings.LSTM_BATCH_SIZE,
            validation_split=0.1,
            verbose=0,
        )

        reconstructions = self.model.predict(seqs, verbose=0)
        mse = np.mean(np.power(seqs - reconstructions, 2), axis=(1, 2))
        self.threshold = float(np.percentile(mse, 95))

        save_model({
            "weights": self.model.get_weights(),
            "scaler": self._scaler,
            "threshold": self.threshold,
            "seq_len": self._seq_len,
            "n_features": n_features,
            "latent_dim": settings.LSTM_LATENT_DIM,
            "features": used,
        }, self._model_key)
        logger.info("LSTM Autoencoder trained for device %s — threshold %.4f", self.device_id, self.threshold)

    def predict(self, df: pd.DataFrame) -> list[dict]:
        artifact = load_model(self._model_key)
        if artifact is None:
            logger.warning("No saved LSTM model for %s — training on-the-fly", self.device_id)
            self.train(df)
            return self.predict(df)

        self._scaler = artifact["scaler"]
        threshold = artifact["threshold"]
        seq_len = artifact["seq_len"]
        n_features = artifact["n_features"]
        used_features: list[str] = artifact["features"]

        model = _build_autoencoder(seq_len, n_features, artifact["latent_dim"])
        model.set_weights(artifact["weights"])

        X, _ = self._prepare(df)
        seqs = _make_sequences(X, seq_len)

        reconstructions = model.predict(seqs, verbose=0)
        mse = np.mean(np.power(seqs - reconstructions, 2), axis=(1, 2))

        results = []
        for idx, row in enumerate(df.itertuples()):
            seq_idx = max(0, idx - seq_len + 1)
            score = float(mse[seq_idx]) if seq_idx < len(mse) else 0.0
            is_anomaly = score > threshold
            confidence = float(np.clip(score / (threshold + 1e-9), 0, 1))
            results.append({
                "timestamp": getattr(row, "timestamp", None),
                "device_id": self.device_id,
                "anomaly_score": score,
                "is_anomaly": is_anomaly,
                "confidence": confidence,
                "features_used": used_features,
            })
        return results

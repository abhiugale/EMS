import logging
import uuid

import pandas as pd
from celery import Task

from celery_app import celery_app
from models.isolation_forest import IsolationForestDetector
from models.lstm_autoencoder import LSTMAutoencoder

logger = logging.getLogger(__name__)


class _BaseTask(Task):
    abstract = True

    def on_failure(self, exc, task_id, args, kwargs, einfo):
        logger.error("Task %s failed: %s", task_id, exc, exc_info=einfo)


@celery_app.task(
    bind=True,
    base=_BaseTask,
    name="tasks.anomaly_tasks.run_isolation_forest",
    queue="anomaly",
    max_retries=3,
    default_retry_delay=30,
)
def run_isolation_forest(self, device_id: str, readings: list[dict]) -> dict:
    try:
        df = pd.DataFrame(readings)
        detector = IsolationForestDetector(device_id)
        results = detector.predict(df)
        anomalies = [r for r in results if r["is_anomaly"]]
        return {
            "request_id": str(uuid.uuid4()),
            "device_id": device_id,
            "model_type": "isolation_forest",
            "results": results,
            "total_anomalies": len(anomalies),
            "anomaly_rate": len(anomalies) / len(results) if results else 0.0,
        }
    except Exception as exc:
        logger.exception("IsolationForest task failed for device %s", device_id)
        raise self.retry(exc=exc)


@celery_app.task(
    bind=True,
    base=_BaseTask,
    name="tasks.anomaly_tasks.run_lstm_autoencoder",
    queue="anomaly",
    max_retries=3,
    default_retry_delay=60,
)
def run_lstm_autoencoder(self, device_id: str, readings: list[dict]) -> dict:
    try:
        df = pd.DataFrame(readings)
        detector = LSTMAutoencoder(device_id)
        results = detector.predict(df)
        anomalies = [r for r in results if r["is_anomaly"]]
        return {
            "request_id": str(uuid.uuid4()),
            "device_id": device_id,
            "model_type": "lstm_autoencoder",
            "results": results,
            "total_anomalies": len(anomalies),
            "anomaly_rate": len(anomalies) / len(results) if results else 0.0,
        }
    except Exception as exc:
        logger.exception("LSTM Autoencoder task failed for device %s", device_id)
        raise self.retry(exc=exc)

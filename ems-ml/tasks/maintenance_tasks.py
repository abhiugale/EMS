import logging
import uuid

import pandas as pd
from celery import Task

from celery_app import celery_app
from config import settings
from models.maintenance_rf import MaintenanceRandomForest

logger = logging.getLogger(__name__)


class _BaseTask(Task):
    abstract = True

    def on_failure(self, exc, task_id, args, kwargs, einfo):
        logger.error("Task %s failed: %s", task_id, exc, exc_info=einfo)


@celery_app.task(
    bind=True,
    base=_BaseTask,
    name="tasks.maintenance_tasks.run_maintenance_prediction",
    queue="maintenance",
    max_retries=3,
    default_retry_delay=30,
)
def run_maintenance_prediction(self, device_id: str, readings: list[dict]) -> dict:
    try:
        df = pd.DataFrame(readings)
        predictor = MaintenanceRandomForest(device_id)
        result = predictor.predict(df)
        alert = result["failure_probability"] >= settings.MAINTENANCE_ALERT_THRESHOLD
        return {
            "request_id": str(uuid.uuid4()),
            "result": result,
            "alert_triggered": alert,
            "alert_threshold": settings.MAINTENANCE_ALERT_THRESHOLD,
        }
    except Exception as exc:
        logger.exception("Maintenance prediction failed for device %s", device_id)
        raise self.retry(exc=exc)

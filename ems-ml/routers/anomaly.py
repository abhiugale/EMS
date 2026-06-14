import uuid
import logging
from typing import Annotated

from celery.result import AsyncResult
from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel

from schemas.anomaly import AnomalyRequest, AnomalyResponse, AnomalyTaskStatus
from tasks.anomaly_tasks import run_isolation_forest, run_lstm_autoencoder

logger = logging.getLogger(__name__)
router = APIRouter()


class TaskQueued(BaseModel):
    task_id: str
    status: str = "PENDING"
    message: str


@router.post("/detect", response_model=TaskQueued, summary="Queue anomaly detection (async)")
async def detect_anomalies(request: AnomalyRequest) -> TaskQueued:
    readings = [r.model_dump(mode="json") for r in request.readings]
    if request.model_type == "lstm_autoencoder":
        task = run_lstm_autoencoder.delay(request.device_id, readings)
    else:
        task = run_isolation_forest.delay(request.device_id, readings)
    return TaskQueued(
        task_id=task.id,
        message=f"Anomaly detection queued with model '{request.model_type}'.",
    )


@router.get("/status/{task_id}", response_model=AnomalyTaskStatus, summary="Poll task status")
async def anomaly_task_status(task_id: str) -> AnomalyTaskStatus:
    result = AsyncResult(task_id)
    status = result.status
    if status == "SUCCESS":
        return AnomalyTaskStatus(task_id=task_id, status=status, result=result.result)
    if status == "FAILURE":
        return AnomalyTaskStatus(task_id=task_id, status=status, error=str(result.result))
    return AnomalyTaskStatus(task_id=task_id, status=status)


@router.get("/models", summary="List available anomaly model types")
async def list_models() -> dict:
    return {"models": ["isolation_forest", "lstm_autoencoder"]}

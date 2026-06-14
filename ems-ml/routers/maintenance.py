import logging
from pydantic import BaseModel

from celery.result import AsyncResult
from fastapi import APIRouter

from schemas.maintenance import MaintenanceRequest, MaintenanceTaskStatus
from tasks.maintenance_tasks import run_maintenance_prediction

logger = logging.getLogger(__name__)
router = APIRouter()


class TaskQueued(BaseModel):
    task_id: str
    status: str = "PENDING"
    message: str


@router.post("/predict", response_model=TaskQueued, summary="Queue maintenance risk prediction (async)")
async def predict_maintenance(request: MaintenanceRequest) -> TaskQueued:
    readings = [r.model_dump(mode="json") for r in request.readings]
    task = run_maintenance_prediction.delay(request.device_id, readings)
    return TaskQueued(
        task_id=task.id,
        message=f"Maintenance prediction queued for device '{request.device_id}'.",
    )


@router.get("/status/{task_id}", response_model=MaintenanceTaskStatus, summary="Poll task status")
async def maintenance_task_status(task_id: str) -> MaintenanceTaskStatus:
    result = AsyncResult(task_id)
    status = result.status
    if status == "SUCCESS":
        return MaintenanceTaskStatus(task_id=task_id, status=status, result=result.result)
    if status == "FAILURE":
        return MaintenanceTaskStatus(task_id=task_id, status=status, error=str(result.result))
    return MaintenanceTaskStatus(task_id=task_id, status=status)


@router.get("/risk-levels", summary="Describe risk level thresholds")
async def risk_levels() -> dict:
    return {
        "LOW": {"range": "0.0 – 0.3", "action": "Routine monitoring"},
        "MEDIUM": {"range": "0.3 – 0.5", "action": "Inspect within 30 days"},
        "HIGH": {"range": "0.5 – 0.7", "action": "Maintain within 7 days"},
        "CRITICAL": {"range": "0.7 – 1.0", "action": "Immediate maintenance required"},
    }

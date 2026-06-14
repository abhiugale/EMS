import logging
from pydantic import BaseModel

from celery.result import AsyncResult
from fastapi import APIRouter

from schemas.forecast import ForecastRequest, ForecastTaskStatus
from tasks.forecast_tasks import run_prophet_forecast, run_xgboost_forecast

logger = logging.getLogger(__name__)
router = APIRouter()


class TaskQueued(BaseModel):
    task_id: str
    status: str = "PENDING"
    message: str


@router.post("/predict", response_model=TaskQueued, summary="Queue load forecast (async)")
async def predict_load(request: ForecastRequest) -> TaskQueued:
    readings = [r.model_dump(mode="json") for r in request.historical_readings]
    if request.model_type == "xgboost":
        task = run_xgboost_forecast.delay(request.device_id, readings, request.horizon_hours)
    else:
        task = run_prophet_forecast.delay(request.device_id, readings, request.horizon_hours)
    return TaskQueued(
        task_id=task.id,
        message=f"Forecast queued with model '{request.model_type}' for {request.horizon_hours}h horizon.",
    )


@router.get("/status/{task_id}", response_model=ForecastTaskStatus, summary="Poll task status")
async def forecast_task_status(task_id: str) -> ForecastTaskStatus:
    result = AsyncResult(task_id)
    status = result.status
    if status == "SUCCESS":
        return ForecastTaskStatus(task_id=task_id, status=status, result=result.result)
    if status == "FAILURE":
        return ForecastTaskStatus(task_id=task_id, status=status, error=str(result.result))
    return ForecastTaskStatus(task_id=task_id, status=status)


@router.get("/models", summary="List available forecast model types")
async def list_models() -> dict:
    return {"models": ["prophet", "xgboost"]}

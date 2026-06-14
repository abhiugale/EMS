from datetime import datetime
from typing import Literal
from pydantic import BaseModel, Field, ConfigDict


class HistoricalReading(BaseModel):
    timestamp: datetime
    device_id: str
    power_kw: float = Field(..., ge=0)
    temperature_c: float | None = None
    is_holiday: bool = False


class ForecastRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={"example": {
        "device_id": "METER-001",
        "historical_readings": [],
        "horizon_hours": 24,
        "model_type": "prophet",
    }})

    device_id: str
    historical_readings: list[HistoricalReading] = Field(..., min_length=48)
    horizon_hours: int = Field(24, ge=1, le=168)
    model_type: Literal["prophet", "xgboost"] = "prophet"
    include_confidence_interval: bool = True


class ForecastPoint(BaseModel):
    timestamp: datetime
    predicted_kw: float
    lower_bound_kw: float | None = None
    upper_bound_kw: float | None = None


class ForecastResponse(BaseModel):
    request_id: str
    device_id: str
    model_type: str
    horizon_hours: int
    forecast: list[ForecastPoint]
    mae: float | None = None
    rmse: float | None = None


class ForecastTaskStatus(BaseModel):
    task_id: str
    status: Literal["PENDING", "STARTED", "SUCCESS", "FAILURE", "RETRY"]
    result: ForecastResponse | None = None
    error: str | None = None

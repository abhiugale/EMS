from datetime import datetime
from typing import Literal
from pydantic import BaseModel, Field, ConfigDict


class EquipmentReading(BaseModel):
    timestamp: datetime
    device_id: str
    runtime_hours: float = Field(..., ge=0)
    power_kw: float = Field(..., ge=0)
    voltage_v: float | None = None
    current_a: float | None = None
    temperature_c: float | None = None
    vibration_ms2: float | None = None
    last_maintenance_days: int | None = Field(None, ge=0)


class MaintenanceRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={"example": {
        "device_id": "HVAC-003",
        "readings": [],
    }})

    device_id: str
    readings: list[EquipmentReading] = Field(..., min_length=1)


class MaintenanceResult(BaseModel):
    device_id: str
    failure_probability: float = Field(..., ge=0, le=1)
    predicted_rul_days: float | None = None
    risk_level: Literal["LOW", "MEDIUM", "HIGH", "CRITICAL"]
    recommended_action: str
    top_risk_factors: list[str] = []


class MaintenanceResponse(BaseModel):
    request_id: str
    result: MaintenanceResult
    alert_triggered: bool
    alert_threshold: float


class MaintenanceTaskStatus(BaseModel):
    task_id: str
    status: Literal["PENDING", "STARTED", "SUCCESS", "FAILURE", "RETRY"]
    result: MaintenanceResponse | None = None
    error: str | None = None

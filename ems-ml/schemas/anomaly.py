from datetime import datetime
from typing import Literal
from pydantic import BaseModel, Field, ConfigDict


class SensorReading(BaseModel):
    timestamp: datetime
    device_id: str
    power_kw: float = Field(..., ge=0)
    voltage_v: float | None = None
    current_a: float | None = None
    temperature_c: float | None = None
    power_factor: float | None = Field(None, ge=0, le=1)


class AnomalyRequest(BaseModel):
    model_config = ConfigDict(json_schema_extra={"example": {
        "device_id": "METER-001",
        "readings": [{"timestamp": "2025-01-01T00:00:00Z", "device_id": "METER-001", "power_kw": 45.2}],
        "model_type": "isolation_forest",
    }})

    device_id: str
    readings: list[SensorReading] = Field(..., min_length=1)
    model_type: Literal["isolation_forest", "lstm_autoencoder"] = "isolation_forest"


class AnomalyResult(BaseModel):
    timestamp: datetime
    device_id: str
    anomaly_score: float
    is_anomaly: bool
    confidence: float = Field(..., ge=0, le=1)
    features_used: list[str] = []


class AnomalyResponse(BaseModel):
    request_id: str
    device_id: str
    model_type: str
    results: list[AnomalyResult]
    total_anomalies: int
    anomaly_rate: float


class AnomalyTaskStatus(BaseModel):
    task_id: str
    status: Literal["PENDING", "STARTED", "SUCCESS", "FAILURE", "RETRY"]
    result: AnomalyResponse | None = None
    error: str | None = None

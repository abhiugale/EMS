import uuid
from sqlalchemy import Column, String, Numeric, DateTime, ForeignKey, Boolean
from sqlalchemy.dialects.postgresql import UUID
from database import Base

class Factory(Base):
    __tablename__ = "factories"
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    name = Column(String(100), nullable=False)
    timezone = Column(String(50), default="Asia/Kolkata")
    tariff_inr_per_kwh = Column(Numeric, nullable=False)

class Machine(Base):
    __tablename__ = "machines"
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    factory_id = Column(UUID(as_uuid=True), ForeignKey("factories.id"), nullable=False)
    name = Column(String(100), nullable=False)
    baseline_kwh = Column(Numeric, nullable=False)
    is_active = Column(Boolean, default=True)

class EnergyReading(Base):
    __tablename__ = "energy_readings"
    id = Column(Numeric, primary_key=True)
    recorded_at = Column(DateTime(timezone=True), primary_key=True)
    machine_id = Column(UUID(as_uuid=True), ForeignKey("machines.id"), nullable=False)
    energy_kwh = Column(Numeric)
    active_kw = Column(Numeric)
    parts_produced = Column(Numeric)
    upload_id = Column(UUID(as_uuid=True))

class Alert(Base):
    __tablename__ = "alerts"
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    factory_id = Column(UUID(as_uuid=True), ForeignKey("factories.id"), nullable=False)
    machine_id = Column(UUID(as_uuid=True), ForeignKey("machines.id"))
    alert_type = Column(String(50), nullable=False)
    severity = Column(String(20), nullable=False)
    message = Column(String, nullable=False)
    status = Column(String(20), default="OPEN")
    threshold_value = Column(Numeric)
    actual_value = Column(Numeric)
    created_at = Column(DateTime(timezone=True))

class AiInsight(Base):
    __tablename__ = "ai_insights"
    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    factory_id = Column(UUID(as_uuid=True), ForeignKey("factories.id"), nullable=False)
    machine_id = Column(UUID(as_uuid=True), ForeignKey("machines.id"))
    insight_type = Column(String(50), nullable=False)
    message = Column(String, nullable=False)
    savings_potential_kwh = Column(Numeric)
    savings_potential_inr = Column(Numeric)
    status = Column(String(20), default="OPEN")
    created_at = Column(DateTime(timezone=True))

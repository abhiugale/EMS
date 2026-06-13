import numpy as np
import pandas as pd
from sklearn.ensemble import IsolationForest
from sqlalchemy.orm import Session
from models import EnergyReading, Alert, Machine
from datetime import datetime, timezone
import uuid

def detect_anomalies(db: Session, machine_id: uuid.UUID, factory_id: uuid.UUID, readings_df: pd.DataFrame):
    """
    Train Isolation Forest model on historical data, then detect anomalies in the input dataframe.
    """
    if readings_df.empty or "active_kw" not in readings_df.columns:
        return

    # Fetch historical readings to train Isolation Forest
    hist_readings = db.query(EnergyReading.active_kw).filter(
        EnergyReading.machine_id == machine_id
    ).limit(1000).all()

    # If we don't have enough history, use the uploaded readings to train
    if len(hist_readings) < 10:
        hist_data = readings_df[["active_kw"]].dropna()
    else:
        hist_data = pd.DataFrame([float(r.active_kw) for r in hist_readings if r.active_kw is not None], columns=["active_kw"])

    if hist_data.empty:
        return

    # Train Isolation Forest
    model = IsolationForest(contamination=0.05, random_state=42)
    model.fit(hist_data)

    # Predict on current readings
    current_data = readings_df[["active_kw"]].fillna(0)
    predictions = model.predict(current_data)

    # Fetch machine baseline
    machine = db.query(Machine).filter(Machine.id == machine_id).first()
    baseline = float(machine.baseline_kwh) if machine else 0.0

    # Generate alerts
    for idx, row in readings_df.iterrows():
        if predictions[idx] == -1:
            val = float(row["active_kw"])
            if val > 1.0:
                alert = Alert(
                    id=uuid.uuid4(),
                    factory_id=factory_id,
                    machine_id=machine_id,
                    alert_type="ANOMALY",
                    severity="WARNING" if val < baseline * 2 else "CRITICAL",
                    message=f"Anomalous active load of {val:.2f} kW detected on machine '{machine.name if machine else ''}' (baseline: {baseline:.2f} kWh)",
                    status="OPEN",
                    threshold_value=baseline,
                    actual_value=val,
                    created_at=row["recorded_at"] if "recorded_at" in row else datetime.now(timezone.utc)
                )
                db.add(alert)
    db.commit()

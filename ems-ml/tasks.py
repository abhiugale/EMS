from celery_app import app
from database import SessionLocal
from models import EnergyReading, Machine
import pandas as pd
from anomaly_detector import detect_anomalies
from forecaster import generate_forecasts
import uuid
import logging

logger = logging.getLogger(__name__)

@app.task(name="tasks.process_upload_task")
def process_upload_task(upload_id_str):
    logger.info(f"Starting Celery ML processing task for upload: {upload_id_str}")
    upload_id = uuid.UUID(upload_id_str)

    db = SessionLocal()
    try:
        # Fetch all readings for this upload
        readings = db.query(EnergyReading).filter(EnergyReading.upload_id == upload_id).all()
        if not readings:
            logger.warning(f"No readings found for upload {upload_id_str}")
            return

        # Get factory ID from the machine
        first_reading = readings[0]
        machine = db.query(Machine).filter(Machine.id == first_reading.machine_id).first()
        if not machine:
            logger.error(f"Machine not found for ID: {first_reading.machine_id}")
            return
        factory_id = machine.factory_id

        # Group readings by machine
        readings_by_machine = {}
        for r in readings:
            if r.machine_id not in readings_by_machine:
                readings_by_machine[r.machine_id] = []
            readings_by_machine[r.machine_id].append(r)

        # Process each machine's data
        for machine_id, machine_readings in readings_by_machine.items():
            # Convert to DataFrame
            data = []
            for r in machine_readings:
                data.append({
                    "recorded_at": r.recorded_at,
                    "active_kw": float(r.active_kw) if r.active_kw is not None else 0.0,
                    "energy_kwh": float(r.energy_kwh) if r.energy_kwh is not None else 0.0
                })
            df = pd.DataFrame(data)

            logger.info(f"Processing anomaly detection for machine: {machine_id}")
            detect_anomalies(db, machine_id, factory_id, df)

            logger.info(f"Generating forecasting models for machine: {machine_id}")
            generate_forecasts(db, machine_id, factory_id)

    except Exception as e:
        logger.error(f"Error executing Celery task for upload {upload_id_str}: {str(e)}", exc_info=True)
    finally:
        db.close()

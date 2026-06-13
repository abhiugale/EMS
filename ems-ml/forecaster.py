import pandas as pd
from prophet import Prophet
from sqlalchemy.orm import Session
from models import EnergyReading, AiInsight, Machine
from datetime import datetime, timezone
import uuid

def generate_forecasts(db: Session, machine_id: uuid.UUID, factory_id: uuid.UUID):
    """
    Train Prophet forecasting model on historical data, forecast next 7 days, 
    and identify energy shedding opportunities.
    """
    # Fetch historical energy data for the machine
    readings = db.query(EnergyReading.recorded_at, EnergyReading.energy_kwh).filter(
        EnergyReading.machine_id == machine_id
    ).order_by(EnergyReading.recorded_at.asc()).all()

    if len(readings) < 50:
        # Not enough history to forecast
        return

    # Prepare data for Prophet
    df = pd.DataFrame([(r.recorded_at, float(r.energy_kwh)) for r in readings if r.energy_kwh is not None], columns=["ds", "y"])

    # Strip timezone info as Prophet expects timezone-naive timestamps
    df["ds"] = df["ds"].dt.tz_localize(None)

    # Fit Prophet Model
    m = Prophet(daily_seasonality=True, weekly_seasonality=True, yearly_seasonality=False)
    m.fit(df)

    # Predict next 7 days (168 hourly points)
    future = m.make_future_dataframe(periods=168, freq='h')
    forecast = m.predict(future)

    future_forecast = forecast[forecast["ds"] > df["ds"].max()]
    if future_forecast.empty:
        return

    # Analyze peaks to identify shedding opportunities
    peak_row = future_forecast.loc[future_forecast["yhat"].idxmax()]
    peak_time = peak_row["ds"]
    peak_val = peak_row["yhat"]

    machine = db.query(Machine).filter(Machine.id == machine_id).first()
    machine_name = machine.name if machine else "Machine"

    # Save shedding opportunity insight if peak demand is significant
    if peak_val > 10.0:
        savings_kwh = peak_val * 0.15
        savings_inr = savings_kwh * 8.0

        insight = AiInsight(
            id=uuid.uuid4(),
            factory_id=factory_id,
            machine_id=machine_id,
            insight_type="SHEDDING_OPPORTUNITY",
            message=f"Peak energy demand of {peak_val:.2f} kWh forecast on '{machine_name}' at {peak_time.strftime('%Y-%m-%d %H:%M')}. Load shedding can save up to {savings_kwh:.2f} kWh.",
            savings_potential_kwh=savings_kwh,
            savings_potential_inr=savings_inr,
            status="OPEN",
            created_at=datetime.now(timezone.utc)
        )
        db.add(insight)
        db.commit()

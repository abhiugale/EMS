import logging
import uuid

import numpy as np
import pandas as pd
from celery import Task

from celery_app import celery_app
from models.prophet_forecast import ProphetForecaster

logger = logging.getLogger(__name__)


class _BaseTask(Task):
    abstract = True

    def on_failure(self, exc, task_id, args, kwargs, einfo):
        logger.error("Task %s failed: %s", task_id, exc, exc_info=einfo)


@celery_app.task(
    bind=True,
    base=_BaseTask,
    name="tasks.forecast_tasks.run_prophet_forecast",
    queue="forecast",
    max_retries=3,
    default_retry_delay=30,
    time_limit=300,
)
def run_prophet_forecast(
    self,
    device_id: str,
    historical_readings: list[dict],
    horizon_hours: int = 24,
) -> dict:
    try:
        df = pd.DataFrame(historical_readings)
        forecaster = ProphetForecaster(device_id)
        forecast = forecaster.predict(df, horizon_hours)
        return {
            "request_id": str(uuid.uuid4()),
            "device_id": device_id,
            "model_type": "prophet",
            "horizon_hours": horizon_hours,
            "forecast": forecast,
            "mae": None,
            "rmse": None,
        }
    except Exception as exc:
        logger.exception("Prophet forecast failed for device %s", device_id)
        raise self.retry(exc=exc)


@celery_app.task(
    bind=True,
    base=_BaseTask,
    name="tasks.forecast_tasks.run_xgboost_forecast",
    queue="forecast",
    max_retries=3,
    default_retry_delay=30,
    time_limit=300,
)
def run_xgboost_forecast(
    self,
    device_id: str,
    historical_readings: list[dict],
    horizon_hours: int = 24,
) -> dict:
    try:
        import xgboost as xgb
        from sklearn.model_selection import train_test_split
        from utils.feature_engineering import prepare_features
        from utils.model_persistence import save_model, load_model

        df = pd.DataFrame(historical_readings)
        df = prepare_features(df)

        feature_cols = [c for c in df.columns if c not in ("timestamp", "device_id", "power_kw")]
        X = df[feature_cols].fillna(0).values
        y = df["power_kw"].values

        X_train, X_val, y_train, y_val = train_test_split(X, y, test_size=0.1, shuffle=False)

        model = xgb.XGBRegressor(
            n_estimators=200,
            learning_rate=0.05,
            max_depth=6,
            subsample=0.8,
            colsample_bytree=0.8,
            objective="reg:squarederror",
            random_state=42,
        )
        model.fit(X_train, y_train, eval_set=[(X_val, y_val)], verbose=False)

        val_preds = model.predict(X_val)
        mae = float(np.mean(np.abs(val_preds - y_val)))
        rmse = float(np.sqrt(np.mean((val_preds - y_val) ** 2)))

        # Recursive multi-step forecast using last row as seed
        last_row = df[feature_cols].iloc[-1].values.copy()
        forecast = []
        last_ts = pd.to_datetime(df["timestamp"].max(), utc=True)
        for h in range(1, horizon_hours + 1):
            pred = float(max(0.0, model.predict(last_row.reshape(1, -1))[0]))
            ts = last_ts + pd.Timedelta(hours=h)
            forecast.append({"timestamp": ts.isoformat(), "predicted_kw": pred,
                             "lower_bound_kw": None, "upper_bound_kw": None})
            last_row[0] = pred  # naive update of first lag feature

        return {
            "request_id": str(uuid.uuid4()),
            "device_id": device_id,
            "model_type": "xgboost",
            "horizon_hours": horizon_hours,
            "forecast": forecast,
            "mae": mae,
            "rmse": rmse,
        }
    except Exception as exc:
        logger.exception("XGBoost forecast failed for device %s", device_id)
        raise self.retry(exc=exc)

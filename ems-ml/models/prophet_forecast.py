import logging

import numpy as np
import pandas as pd

from config import settings
from utils.model_persistence import save_model, load_model

logger = logging.getLogger(__name__)


class ProphetForecaster:
    def __init__(self, device_id: str):
        self.device_id = device_id
        self._model_key = f"prophet_{device_id}"

    def train(self, df: pd.DataFrame) -> None:
        from prophet import Prophet

        train_df = df[["timestamp", "power_kw"]].rename(columns={"timestamp": "ds", "power_kw": "y"})
        train_df["ds"] = pd.to_datetime(train_df["ds"], utc=True).dt.tz_localize(None)

        model = Prophet(
            seasonality_mode=settings.PROPHET_SEASONALITY_MODE,
            daily_seasonality=True,
            weekly_seasonality=True,
            yearly_seasonality=False,
            interval_width=0.95,
        )
        if "temperature_c" in df.columns:
            model.add_regressor("temperature_c")
            train_df["temperature_c"] = df["temperature_c"].values
        if "is_holiday" in df.columns:
            model.add_regressor("is_holiday")
            train_df["is_holiday"] = df["is_holiday"].astype(int).values

        model.fit(train_df)
        save_model({"model": model}, self._model_key)
        logger.info("Prophet model trained for device %s", self.device_id)

    def predict(self, df: pd.DataFrame, horizon_hours: int) -> list[dict]:
        artifact = load_model(self._model_key)
        if artifact is None:
            logger.warning("No Prophet model for %s — training on-the-fly", self.device_id)
            self.train(df)
            artifact = load_model(self._model_key)

        from prophet import Prophet
        model: Prophet = artifact["model"]

        last_ts = pd.to_datetime(df["timestamp"].max(), utc=True).tz_localize(None)
        future = model.make_future_dataframe(periods=horizon_hours, freq="h", include_history=False)

        forecast = model.predict(future)
        results = []
        for _, row in forecast.iterrows():
            results.append({
                "timestamp": row["ds"].isoformat() + "Z",
                "predicted_kw": max(0.0, float(row["yhat"])),
                "lower_bound_kw": max(0.0, float(row["yhat_lower"])),
                "upper_bound_kw": max(0.0, float(row["yhat_upper"])),
            })
        return results

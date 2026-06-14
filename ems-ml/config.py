from pydantic_settings import BaseSettings, SettingsConfigDict
from functools import lru_cache


class Settings(BaseSettings):
    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8", extra="ignore")

    # Application
    APP_NAME: str = "EMS ML Service"
    APP_VERSION: str = "1.0.0"
    DEBUG: bool = False

    # Database
    DATABASE_URL: str = "postgresql+asyncpg://postgres:postgres@localhost:5432/emsdb"
    DB_POOL_SIZE: int = 10
    DB_MAX_OVERFLOW: int = 20
    DB_POOL_TIMEOUT: int = 30

    # Redis / Celery
    REDIS_URL: str = "redis://localhost:6379/0"
    CELERY_BROKER_URL: str = "redis://localhost:6379/0"
    CELERY_RESULT_BACKEND: str = "redis://localhost:6379/1"

    # CORS
    ALLOWED_ORIGINS: list[str] = [
        "http://localhost:5173",
        "http://localhost:8080",
    ]

    # Model persistence
    MODEL_DIR: str = "saved_models"
    MODEL_RETRAIN_INTERVAL_HOURS: int = 24

    # Anomaly detection
    ANOMALY_CONTAMINATION: float = 0.05
    ANOMALY_SCORE_THRESHOLD: float = -0.2
    LSTM_SEQUENCE_LENGTH: int = 24
    LSTM_EPOCHS: int = 50
    LSTM_BATCH_SIZE: int = 32
    LSTM_LATENT_DIM: int = 64

    # Forecasting
    FORECAST_HORIZON_HOURS: int = 24
    PROPHET_SEASONALITY_MODE: str = "multiplicative"
    XGBOOST_N_ESTIMATORS: int = 200

    # Maintenance
    RF_N_ESTIMATORS: int = 100
    RF_MAX_DEPTH: int = 10
    MAINTENANCE_ALERT_THRESHOLD: float = 0.7


@lru_cache
def get_settings() -> Settings:
    return Settings()


settings = get_settings()

import numpy as np
import pandas as pd


def add_time_features(df: pd.DataFrame, timestamp_col: str = "timestamp") -> pd.DataFrame:
    """Augment a dataframe with cyclical time encodings and boolean flags."""
    df = df.copy()
    ts = pd.to_datetime(df[timestamp_col], utc=True)
    df["hour"] = ts.dt.hour
    df["day_of_week"] = ts.dt.dayofweek
    df["month"] = ts.dt.month
    df["is_weekend"] = (df["day_of_week"] >= 5).astype(int)
    # Cyclical encodings
    df["hour_sin"] = np.sin(2 * np.pi * df["hour"] / 24)
    df["hour_cos"] = np.cos(2 * np.pi * df["hour"] / 24)
    df["dow_sin"] = np.sin(2 * np.pi * df["day_of_week"] / 7)
    df["dow_cos"] = np.cos(2 * np.pi * df["day_of_week"] / 7)
    return df


def add_rolling_stats(
    df: pd.DataFrame,
    col: str = "power_kw",
    windows: list[int] | None = None,
) -> pd.DataFrame:
    """Add rolling mean, std, and z-score features for a given column."""
    if windows is None:
        windows = [4, 12, 24]
    df = df.copy()
    for w in windows:
        df[f"{col}_rolling_mean_{w}h"] = df[col].rolling(w, min_periods=1).mean()
        df[f"{col}_rolling_std_{w}h"] = df[col].rolling(w, min_periods=1).std().fillna(0)
    mu = df[f"{col}_rolling_mean_24h"]
    sigma = df[f"{col}_rolling_std_24h"].replace(0, 1)
    df[f"{col}_zscore"] = (df[col] - mu) / sigma
    return df


def add_lag_features(
    df: pd.DataFrame,
    col: str = "power_kw",
    lags: list[int] | None = None,
) -> pd.DataFrame:
    if lags is None:
        lags = [1, 4, 24, 48]
    df = df.copy()
    for lag in lags:
        df[f"{col}_lag_{lag}h"] = df[col].shift(lag)
    return df.ffill().fillna(0)


def add_rate_of_change(df: pd.DataFrame, col: str = "power_kw") -> pd.DataFrame:
    df = df.copy()
    df[f"{col}_roc_1h"] = df[col].diff(1).fillna(0)
    df[f"{col}_roc_4h"] = df[col].diff(4).fillna(0)
    return df


def prepare_features(df: pd.DataFrame) -> pd.DataFrame:
    """Full feature engineering pipeline."""
    df = add_time_features(df)
    df = add_rolling_stats(df)
    df = add_lag_features(df)
    df = add_rate_of_change(df)
    return df

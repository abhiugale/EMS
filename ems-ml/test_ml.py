import pytest
from fastapi.testclient import TestClient
from main import app
import numpy as np
import pandas as pd
from datetime import datetime, timedelta

client = TestClient(app)

def test_health():
    response = client.get("/health")
    assert response.status_code == 200
    assert response.json() == {"status": "healthy"}

def test_anomaly_data_prep():
    # Verify basic data format for training
    df = pd.DataFrame({
        "recorded_at": [datetime.now() - timedelta(hours=i) for i in range(50)],
        "active_kw": np.random.normal(50.0, 2.0, 50),
        "energy_kwh": np.random.normal(48.0, 1.5, 50)
    })
    assert len(df) == 50
    assert "active_kw" in df.columns

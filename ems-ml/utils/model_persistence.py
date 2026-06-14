import logging
from pathlib import Path
from typing import Any

import joblib

from config import settings

logger = logging.getLogger(__name__)
_BASE = Path(settings.MODEL_DIR)


def _path(key: str) -> Path:
    _BASE.mkdir(parents=True, exist_ok=True)
    return _BASE / f"{key}.joblib"


def save_model(artifact: Any, key: str) -> Path:
    p = _path(key)
    joblib.dump(artifact, p, compress=3)
    logger.info("Model saved → %s", p)
    return p


def load_model(key: str) -> Any | None:
    p = _path(key)
    if not p.exists():
        logger.debug("No model file at %s", p)
        return None
    try:
        return joblib.load(p)
    except Exception as exc:
        logger.error("Failed to load model from %s: %s", p, exc)
        return None


def delete_model(key: str) -> bool:
    p = _path(key)
    if p.exists():
        p.unlink()
        logger.info("Deleted model %s", p)
        return True
    return False


def list_models() -> list[str]:
    _BASE.mkdir(parents=True, exist_ok=True)
    return [f.stem for f in _BASE.glob("*.joblib")]

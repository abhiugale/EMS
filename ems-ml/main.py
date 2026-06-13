from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from tasks import process_upload_task
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="EMS Machine Learning Service")

class BatchRequest(BaseModel):
    upload_id: str

@app.post("/predict/batch")
def predict_batch(request: BatchRequest):
    try:
        task = process_upload_task.delay(request.upload_id)
        logger.info(f"Queued Celery task {task.id} for upload {request.upload_id}")
        return {"status": "queued", "task_id": task.id}
    except Exception as e:
        logger.error(f"Failed to queue celery task: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Failed to queue task: {str(e)}")

@app.get("/health")
def health():
    return {"status": "healthy"}

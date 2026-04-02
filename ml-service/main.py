"""
Self-Healing E-Commerce Platform — ML Anomaly Detection Service
Entry point: starts FastAPI server + Kafka consumer in background thread
"""
import threading
import uvicorn
from app.api.routes import app
from app.kafka.consumer import start_consumer
from app.core.logger import get_logger

logger = get_logger(__name__)

if __name__ == "__main__":
    logger.info("Starting ML Anomaly Detection Service")

    # Start Kafka consumer in background thread
    consumer_thread = threading.Thread(target=start_consumer, daemon=True)
    consumer_thread.start()
    logger.info("Kafka consumer thread started")

    # Start FastAPI
    uvicorn.run(app, host="0.0.0.0", port=8090, log_level="info")

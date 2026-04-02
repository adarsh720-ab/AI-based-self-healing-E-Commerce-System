import os

class Settings:
    KAFKA_BOOTSTRAP: str        = os.getenv("KAFKA_BOOTSTRAP",    "localhost:9092")
    KAFKA_INPUT_TOPIC: str      = os.getenv("KAFKA_INPUT_TOPIC",  "service-logs")
    KAFKA_OUTPUT_TOPIC: str     = os.getenv("KAFKA_OUTPUT_TOPIC", "anomaly-events")
    KAFKA_GROUP_ID: str         = os.getenv("KAFKA_GROUP_ID",     "ml-anomaly-detector")
    KAFKA_AUTO_OFFSET: str      = os.getenv("KAFKA_AUTO_OFFSET",  "earliest")
    MODEL_DIR: str              = os.getenv("MODEL_DIR",          "app/model")
    ANOMALY_THRESHOLD: float    = float(os.getenv("ANOMALY_THRESHOLD", "-0.05"))
    SERVICE_PORT: int           = int(os.getenv("SERVICE_PORT",   "8090"))
    ENVIRONMENT: str            = os.getenv("ENVIRONMENT",        "prod")
    MAX_ANOMALY_HISTORY: int    = int(os.getenv("MAX_ANOMALY_HISTORY", "1000"))

settings = Settings()
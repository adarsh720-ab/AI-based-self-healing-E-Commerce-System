import json
from kafka import KafkaProducer
from app.core.config import settings
from app.core.logger import get_logger

logger = get_logger(__name__)
_producer = None

def get_producer() -> KafkaProducer:
    global _producer
    if _producer is None:
        _producer = KafkaProducer(
            bootstrap_servers=settings.KAFKA_BOOTSTRAP,
            value_serializer=lambda v: json.dumps(v).encode("utf-8"),
            key_serializer=lambda k: k.encode("utf-8") if k else None,
            acks="all",
            retries=3,
            max_block_ms=5000
        )
        logger.info(f"Kafka producer connected → {settings.KAFKA_BOOTSTRAP}")
    return _producer

def publish_anomaly(anomaly_event: dict):
    try:
        producer   = get_producer()
        service_id = anomaly_event.get("serviceId", "unknown")
        producer.send(
            settings.KAFKA_OUTPUT_TOPIC,
            key=service_id,
            value=anomaly_event
        )
        producer.flush()
        logger.info(
            f"Published anomaly → topic={settings.KAFKA_OUTPUT_TOPIC} "
            f"service={service_id} type={anomaly_event.get('anomalyType')} "
            f"confidence={anomaly_event.get('confidence')}"
        )
    except Exception as e:
        logger.error(f"Failed to publish anomaly: {e}")
import json
import time
from datetime import datetime, timezone
from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
from app.core.config import settings
from app.core.logger import get_logger
from app.core.store import anomaly_store
from app.model.predictor import predictor
from app.kafka.producer import publish_anomaly

logger = get_logger(__name__)

def _build_consumer() -> KafkaConsumer:
    for attempt in range(1, 11):
        try:
            consumer = KafkaConsumer(
                settings.KAFKA_INPUT_TOPIC,
                bootstrap_servers=settings.KAFKA_BOOTSTRAP,
                group_id=settings.KAFKA_GROUP_ID,
                auto_offset_reset=settings.KAFKA_AUTO_OFFSET,
                enable_auto_commit=True,
                value_deserializer=lambda v: json.loads(v.decode("utf-8")),
                consumer_timeout_ms=1000,
                session_timeout_ms=30000,
                heartbeat_interval_ms=10000
            )
            logger.info(f"Kafka consumer connected → "
                        f"topic={settings.KAFKA_INPUT_TOPIC} "
                        f"group={settings.KAFKA_GROUP_ID}")
            return consumer
        except NoBrokersAvailable:
            logger.warning(f"Kafka not available (attempt {attempt}/10) — retrying in 5s")
            time.sleep(5)
    raise RuntimeError("Could not connect to Kafka after 10 attempts")

def _process_log(log: dict):
    trace_id   = log.get("traceId", "unknown")
    service_id = log.get("serviceId", "unknown")
    result     = predictor.predict(log)

    if result["isAnomaly"]:
        anomaly_event = {
            "traceId":           trace_id,
            "serviceId":         service_id,
            "method":            log.get("method"),
            "level":             log.get("level"),
            "errorCode":         log.get("errorCode"),
            "message":           log.get("message"),
            "latencyMs":         log.get("latencyMs"),
            "httpMethod":        log.get("httpMethod"),
            "statusCode":        log.get("statusCode"),
            "environment":       log.get("environment"),
            "originalTimestamp": log.get("timestamp"),
            "anomalyScore":      result["anomalyScore"],
            "anomalyType":       result["anomalyType"],
            "confidence":        result["confidence"],
            "detectedAt":        datetime.now(timezone.utc).isoformat(),
            "detectedBy":        "isolation-forest-v2"
        }
        anomaly_store.add(anomaly_event)
        publish_anomaly(anomaly_event)
        logger.warning(
            f"🚨 ANOMALY | service={service_id} | "
            f"type={result['anomalyType']} | "
            f"confidence={result['confidence']} | "
            f"score={result['anomalyScore']} | "
            f"trace={trace_id}"
        )
    else:
        logger.debug(f"✅ NORMAL  | service={service_id} | "
                     f"score={result['anomalyScore']:.4f} | trace={trace_id}")

def start_consumer():
    logger.info("Kafka consumer loop starting...")
    while True:
        try:
            consumer = _build_consumer()
            logger.info("Listening for logs...")
            for message in consumer:
                try:
                    log = message.value
                    if not isinstance(log, dict):
                        logger.warning(f"Skipping non-dict message: {type(log)}")
                        continue
                    if "serviceId" not in log or "level" not in log:
                        logger.warning(f"Skipping incomplete log: {log}")
                        continue
                    _process_log(log)
                except Exception as e:
                    logger.error(f"Error processing message: {e}")
                    continue
        except Exception as e:
            logger.error(f"Consumer error: {e} — reconnecting in 10s")
            time.sleep(10)
import json
import joblib
import numpy as np
from pathlib import Path
from typing import Dict, Any
from app.core.config import settings
from app.core.logger import get_logger

logger = get_logger(__name__)

class AnomalyPredictor:

    def __init__(self):
        self.model      = None
        self.scaler     = None
        self.le_service = None
        self.le_http    = None
        self.meta       = None
        self._load()

    def _load(self):
        model_dir = Path(settings.MODEL_DIR)
        logger.info(f"Loading model artifacts from {model_dir}")
        self.model      = joblib.load(model_dir / "isolation_forest.pkl")
        self.scaler     = joblib.load(model_dir / "scaler.pkl")
        self.le_service = joblib.load(model_dir / "label_encoder_service.pkl")
        self.le_http    = joblib.load(model_dir / "label_encoder_http.pkl")
        with open(model_dir / "model_meta.json") as f:
            self.meta = json.load(f)
        logger.info(f"Model v{self.meta['version']} loaded — "
                    f"{self.meta['n_estimators']} trees, "
                    f"{len(self.meta['features'])} features")

    def _encode_service(self, service_id: str) -> int:
        classes = list(self.le_service.classes_)
        return classes.index(service_id) if service_id in classes else 0

    def _encode_http(self, http_method: str) -> int:
        classes = list(self.le_http.classes_)
        return classes.index(http_method) if http_method in classes else 0

    def _error_severity(self, error_code: str) -> int:
        if not error_code: return 0
        if "Retryable" in error_code or "Timeout" in error_code: return 3
        if "InsufficientStock" in error_code or "PaymentFailed" in error_code: return 2
        return 1

    def _status_category(self, status_code: int) -> int:
        if status_code >= 500: return 3
        if status_code >= 400: return 1
        return 0

    def _build_features(self, log: Dict[str, Any]) -> np.ndarray:
        level       = log.get("level", "INFO")
        error_code  = log.get("errorCode")
        stack_trace = log.get("stackTrace")
        service_id  = log.get("serviceId", "auth-service")
        latency     = float(log.get("latencyMs", 0))
        status_code = int(log.get("statusCode", 200))

        features = [
            1 if level == "ERROR" else 0,
            1 if level == "WARN"  else 0,
            1 if error_code  else 0,
            1 if stack_trace else 0,
            self._encode_service(service_id),
            latency,
            min(4, int(latency / 200)),
            self._error_severity(error_code),
            self._status_category(status_code),
            1 if status_code >= 500 else 0,
            1 if status_code == 503 else 0,
        ]
        return np.array(features).reshape(1, -1)

    def predict(self, log: Dict[str, Any]) -> Dict[str, Any]:
        try:
            features   = self._build_features(log)
            scaled     = self.scaler.transform(features)
            raw_pred   = self.model.predict(scaled)[0]
            score      = float(self.model.decision_function(scaled)[0])
            is_anomaly = raw_pred == -1
            return {
                "isAnomaly":    is_anomaly,
                "anomalyScore": round(score, 4),
                "anomalyType":  self._classify_anomaly(log, score) if is_anomaly else None,
                "confidence":   self._confidence(score)            if is_anomaly else None
            }
        except Exception as e:
            logger.error(f"Prediction failed for log {log.get('traceId')}: {e}")
            return {"isAnomaly": False, "anomalyScore": 0.0,
                    "anomalyType": None, "confidence": None}

    def _classify_anomaly(self, log: Dict[str, Any], score: float) -> str:
        status_code = int(log.get("statusCode", 200))
        latency     = float(log.get("latencyMs", 0))
        error_code  = log.get("errorCode", "")
        level       = log.get("level", "INFO")
        if status_code == 503 or (error_code and "Retryable" in str(error_code)):
            return "service_down"
        if status_code == 504 or (error_code and "Timeout" in str(error_code)):
            return "latency_spike_with_errors"
        if latency >= 2000 and level == "INFO":
            return "latency_spike"
        if latency >= 2000 and level == "ERROR":
            return "latency_spike_with_errors"
        if level == "ERROR":
            return "error_spike"
        return "unknown_anomaly"

    def _confidence(self, score: float) -> str:
        if score < -0.10: return "HIGH"
        if score < -0.05: return "MEDIUM"
        return "LOW"

predictor = AnomalyPredictor()
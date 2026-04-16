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
        self.fallback_mode = False
        self._load()

    def _load(self):
        model_dir = Path(settings.MODEL_DIR)
        logger.info(f"Loading model artifacts from {model_dir}")
        required = [
            model_dir / "isolation_forest.pkl",
            model_dir / "scaler.pkl",
            model_dir / "label_encoder_service.pkl",
            model_dir / "label_encoder_http.pkl",
        ]

        if all(path.exists() for path in required):
            self.model      = joblib.load(model_dir / "isolation_forest.pkl")
            self.scaler     = joblib.load(model_dir / "scaler.pkl")
            self.le_service = joblib.load(model_dir / "label_encoder_service.pkl")
            self.le_http    = joblib.load(model_dir / "label_encoder_http.pkl")
            meta_path = model_dir / "model_meta.json"
            if meta_path.exists():
                with open(meta_path) as f:
                    self.meta = json.load(f)
            else:
                self.meta = {"version": "fallback", "n_estimators": 0, "features": []}
            logger.info(f"Model v{self.meta['version']} loaded — "
                        f"{self.meta.get('n_estimators', 0)} trees, "
                        f"{len(self.meta.get('features', []))} features")
            return

        self.fallback_mode = True
        self.meta = {"version": "fallback", "n_estimators": 0, "features": []}
        logger.warning("Model artifacts not found; ML service running in fallback mode")

    def _encode_service(self, service_id: str) -> int:
        if self.fallback_mode or self.le_service is None:
            return 0
        classes = list(self.le_service.classes_)
        return classes.index(service_id) if service_id in classes else 0

    def _encode_http(self, http_method: str) -> int:
        if self.fallback_mode or self.le_http is None:
            return 0
        classes = list(self.le_http.classes_)
        return classes.index(http_method) if http_method in classes else 0

    def _error_severity(self, error_code: str) -> int:
        if not error_code: return 0
        if "Retryable" in str(error_code) or "Timeout" in str(error_code): return 3
        if "InsufficientStock" in str(error_code) or "PaymentFailed" in str(error_code): return 2
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
            if self.fallback_mode:
                return self._fallback_predict(log)

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
            return {
                "isAnomaly":    False,
                "anomalyScore": 0.0,
                "anomalyType":  None,
                "confidence":   None
            }

    def _fallback_predict(self, log: Dict[str, Any]) -> Dict[str, Any]:
        status_code = int(log.get("statusCode", 200))
        latency     = float(log.get("latencyMs", 0))
        error_code  = log.get("errorCode")
        level       = log.get("level", "INFO")

        is_anomaly = (
            status_code >= 500
            or latency >= 2000
            or level == "ERROR"
            or bool(error_code)
        )

        if status_code >= 503:
            score = -0.08
        elif latency >= 2000:
            score = -0.12
        elif status_code >= 400:
            score = -0.04
        elif level == "ERROR":
            score = -0.03
        else:
            score = 0.02

        return {
            "isAnomaly":    is_anomaly,
            "anomalyScore": round(score, 4),
            "anomalyType":  self._classify_anomaly(log, score) if is_anomaly else None,
            "confidence":   self._confidence(score) if is_anomaly else None
        }

    def _classify_anomaly(self, log: Dict[str, Any], score: float) -> str:
        status_code = int(log.get("statusCode", 200))
        latency     = float(log.get("latencyMs", 0))
        error_code  = log.get("errorCode", "")
        level       = log.get("level", "INFO")

        # Service unreachable
        if status_code == 503 or (error_code and "Retryable" in str(error_code)):
            return "service_down"

        # Timeout
        if status_code == 504 or (error_code and "Timeout" in str(error_code)):
            return "latency_spike_with_errors"

        # Pure latency spike — succeeded but very slow
        if latency >= 2000 and level == "INFO":
            return "latency_spike"

        # Slow and failed
        if latency >= 2000 and level == "ERROR":
            return "latency_spike_with_errors"

        # Any error or 4xx
        if level == "ERROR" or status_code >= 400:
            return "error_spike"

        # Moderately slow
        if latency >= 500:
            return "latency_spike"

        # Model flagged it — statistically unusual but no clear pattern
        return "unusual_behavior"

    def _confidence(self, score: float) -> str:
        """
        Thresholds derived from real log score distribution:
        - Service down (503):          -0.045 to -0.073
        - Timeout / latency spike:     -0.10  to -0.13
        - Mild errors (conflict/404):  -0.001 to -0.004
        """
        if score < -0.07:   return "HIGH"      # timeouts, full service down
        if score < -0.03:   return "MEDIUM"    # 503s, connection refused bursts
        return "LOW"                           # mild errors, single failures


# Singleton — loaded once at startup, reused for every log
predictor = AnomalyPredictor()
import threading
from collections import deque
from typing import List, Dict, Any
from app.core.config import settings

class AnomalyStore:
    def __init__(self):
        self._lock  = threading.Lock()
        self._store = deque(maxlen=settings.MAX_ANOMALY_HISTORY)
        self._counts: Dict[str, int] = {}

    def add(self, anomaly: Dict[str, Any]):
        with self._lock:
            self._store.appendleft(anomaly)
            service = anomaly.get("serviceId", "unknown")
            self._counts[service] = self._counts.get(service, 0) + 1

    def get_all(self, limit: int = 100) -> List[Dict[str, Any]]:
        with self._lock:
            return list(self._store)[:limit]

    def get_by_service(self, service_id: str, limit: int = 50) -> List[Dict[str, Any]]:
        with self._lock:
            return [a for a in self._store if a.get("serviceId") == service_id][:limit]

    def get_counts(self) -> Dict[str, int]:
        with self._lock:
            return dict(self._counts)

    def total(self) -> int:
        with self._lock:
            return len(self._store)

anomaly_store = AnomalyStore()
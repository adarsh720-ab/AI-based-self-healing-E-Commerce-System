from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from datetime import datetime, timezone
from collections import Counter
from app.core.store import anomaly_store

app = FastAPI(
    title="ML Anomaly Detection Service",
    description="Real-time anomaly detection for Self-Healing E-Commerce Platform",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"]
)

@app.get("/health")
def health():
    return {
        "status":    "UP",
        "service":   "ml-anomaly-detection",
        "version":   "2.0.0",
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "totalAnomaliesDetected": anomaly_store.total()
    }

@app.get("/anomalies")
def get_anomalies(limit: int = Query(default=50, le=500)):
    anomalies = anomaly_store.get_all(limit=limit)
    return {"total": len(anomalies), "anomalies": anomalies}

@app.get("/anomalies/stats")
def get_stats():
    counts = anomaly_store.get_counts()
    return {"totalAnomalies": sum(counts.values()), "perService": counts}

@app.get("/anomalies/types")
def get_by_type():
    all_anomalies = anomaly_store.get_all(limit=1000)
    type_counts   = Counter(a.get("anomalyType") for a in all_anomalies)
    return {"totalAnomalies": len(all_anomalies), "perType": dict(type_counts)}

@app.get("/anomalies/{service_id}")
def get_anomalies_by_service(service_id: str, limit: int = Query(default=50, le=200)):
    anomalies = anomaly_store.get_by_service(service_id, limit=limit)
    return {"serviceId": service_id, "total": len(anomalies), "anomalies": anomalies}
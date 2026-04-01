import os
import logging
from typing import Any

from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from qdrant_client import QdrantClient
from qdrant_client.http import models as rest
from sentence_transformers import SentenceTransformer

logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

QDRANT_HOST = os.getenv("QDRANT_HOST", "localhost")
QDRANT_PORT = int(os.getenv("QDRANT_PORT", "6333"))
COLLECTION = os.getenv("QDRANT_COLLECTION", "predmeti")
EMBED_DIM = 384

_client: QdrantClient | None = None
_model: SentenceTransformer | None = None

app = FastAPI(title="NAIS Vector Service")
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)


def get_client() -> QdrantClient:
    global _client
    if _client is None:
        _client = QdrantClient(host=QDRANT_HOST, port=QDRANT_PORT)
    return _client


def get_model() -> SentenceTransformer:
    global _model
    if _model is None:
        _model = SentenceTransformer("all-MiniLM-L6-v2")
    return _model


@app.get("/health")
def health():
    try:
        get_client().get_collections()
        return {"status": "ok", "qdrant": QDRANT_HOST}
    except Exception as e:
        return {"status": "error", "detail": str(e)}


class SearchResult(BaseModel):
    score: float
    payload: dict[str, Any]


@app.get("/api/v1/search")
def search(
    q: str = Query(..., min_length=1),
    limit: int = Query(8, ge=1, le=30),
):
    model = get_model()
    vec = model.encode(q).tolist()
    client = get_client()
    hits = client.search(
        collection_name=COLLECTION,
        query_vector=vec,
        limit=limit,
        with_payload=True,
    )
    results = [
        {"score": float(h.score), "payload": h.payload or {}} for h in hits
    ]
    return {"query": q, "results": results}


def ensure_collection():
    client = get_client()
    cols = {c.name for c in client.get_collections().collections}
    if COLLECTION not in cols:
        client.recreate_collection(
            collection_name=COLLECTION,
            vectors_config=rest.VectorParams(size=EMBED_DIM, distance=rest.Distance.COSINE),
        )
        log.info("Created Qdrant collection %s", COLLECTION)
    else:
        log.info("Qdrant collection %s already exists", COLLECTION)


@app.on_event("startup")
def startup():
    ensure_collection()

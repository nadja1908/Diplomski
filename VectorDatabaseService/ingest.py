"""
Load chunks from /data/chunks.jsonl into Qdrant.
Each line: JSON with text, predmet_id, predmet_sifra, predmet_naziv, tip
"""
import json
import logging
import os
import sys
import time

from qdrant_client import QdrantClient
from qdrant_client.http import models as rest
from sentence_transformers import SentenceTransformer

logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

QDRANT_HOST = os.getenv("QDRANT_HOST", "localhost")
QDRANT_PORT = int(os.getenv("QDRANT_PORT", "6333"))
COLLECTION = os.getenv("QDRANT_COLLECTION", "predmeti")
DATA_PATH = os.getenv("INGEST_PATH", "/data/chunks.jsonl")
EMBED_DIM = 384
BATCH = 64


def main():
    deadline = time.time() + 180
    client = QdrantClient(host=QDRANT_HOST, port=QDRANT_PORT)
    while time.time() < deadline:
        try:
            client.get_collections()
            break
        except Exception as e:
            log.warning("Waiting for Qdrant: %s", e)
            time.sleep(2)
    else:
        log.error("Qdrant not reachable")
        sys.exit(1)

    if not os.path.isfile(DATA_PATH):
        log.warning("No data file at %s — service will start with empty collection", DATA_PATH)
        return

    model = SentenceTransformer("all-MiniLM-L6-v2")
    if COLLECTION in {c.name for c in client.get_collections().collections}:
        client.delete_collection(COLLECTION)
    client.recreate_collection(
        collection_name=COLLECTION,
        vectors_config=rest.VectorParams(size=EMBED_DIM, distance=rest.Distance.COSINE),
    )

    batch_texts: list[str] = []
    batch_payloads: list[dict] = []
    next_id = 0

    def flush():
        nonlocal next_id
        if not batch_texts:
            return
        emb = model.encode(batch_texts)
        pts = [
            rest.PointStruct(
                id=next_id + i,
                vector=emb[i].tolist(),
                payload=batch_payloads[i],
            )
            for i in range(len(batch_texts))
        ]
        client.upsert(COLLECTION, pts)
        next_id += len(pts)
        log.info("Upserted batch, total points: %d", next_id)
        batch_texts.clear()
        batch_payloads.clear()

    with open(DATA_PATH, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line:
                continue
            row = json.loads(line)
            batch_texts.append(row["text"])
            batch_payloads.append(
                {
                    "predmet_id": row.get("predmet_id"),
                    "predmet_sifra": row.get("predmet_sifra", ""),
                    "predmet_naziv": row.get("predmet_naziv", ""),
                    "tip": row.get("tip", "tekst"),
                    "text": row["text"][:4000],
                }
            )
            if len(batch_texts) >= BATCH:
                flush()
        flush()

    log.info("Ingest complete, %d points.", next_id)


if __name__ == "__main__":
    main()

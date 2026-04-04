import os
import re
import math
import logging
from typing import Any

from fastapi import FastAPI, Query
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from qdrant_client import QdrantClient
from qdrant_client.http import models as rest
from sentence_transformers import SentenceTransformer

logging.basicConfig(level=logging.INFO)
log = logging.getLogger(__name__)

QDRANT_HOST = os.getenv("QDRANT_HOST", "localhost")
QDRANT_PORT = int(os.getenv("QDRANT_PORT", "6333"))
COLLECTION = os.getenv("QDRANT_COLLECTION", "predmeti")
EMBEDDING_MODEL = os.getenv("NAIS_EMBEDDING_MODEL", "djovak/embedic-large")
EMBED_DIM = int(os.getenv("NAIS_EMBED_DIM", "1024"))
_preserve_raw = os.getenv("NAIS_EMBED_PRESERVE_DIACRITICS")
if _preserve_raw is not None:
    USE_EMBED_PRESERVE_DIACRITICS = _preserve_raw.lower() in ("1", "true", "yes")
else:
    USE_EMBED_PRESERVE_DIACRITICS = "embedic" in EMBEDDING_MODEL.lower()

# Qdrant vraća kosinusnu sličnost (veće = bliže). Rezovo slabe pogotke daleko od najboljeg
# da generički tekst (Matematika, paralelno programiranje…) ne „lepi“ uz specifična pitanja.
MIN_VECTOR_SIM = float(os.getenv("NAIS_VECTOR_MIN_SIM", "0.22"))
SIM_GAP_FROM_BEST = float(os.getenv("NAIS_VECTOR_SIM_GAP", "0.18"))
# false = ceo upit ide u embedding (bolje za slobodna pitanja); true = samo ključne reči (rizično za SR).
USE_QUERY_FOCUS = os.getenv("NAIS_QUERY_FOCUS", "false").lower() in ("1", "true", "yes")
# Rerang: prvo više pogodaka ključnih reči u payload-u, zatim vektorski skor.
USE_KEYWORD_RERANK = os.getenv("NAIS_KEYWORD_RERANK", "true").lower() in ("1", "true", "yes")

_STOPWORDS = frozenset(
    {
        "the", "what", "which", "how", "when", "where", "who", "why", "does", "did", "are",
        "was", "were", "have", "has", "had", "this", "that", "these", "those", "with", "from",
        "into", "about", "your", "some", "any", "all", "each", "every", "other", "such",
        "koji", "koja", "koje", "koju", "kojem", "kojim", "čiji", "ciji", "šta", "sta",
        "predmet", "predmeta", "predmeti", "predmete", "predmetu", "predmetom",
        "kurs", "kursa", "kursu", "kursevi", "kurseva",
        "sadržaj", "sadrzaj", "sadržaja", "obuhvata", "obuhvataju", "pokriva", "pokrivaju",
        "uključuje", "ukljucuje", "uključuju", "nastave", "nastava", "vezbe", "vežbe",
        "pitanje", "pitanja", "molim", "reci", "navedi", "objasni", "opisi", "opis",
        "mi", "ti", "vi", "on", "ona", "ono", "smo", "ste", "su", "sam", "si", "je",
        "bi", "bih", "bismo", "biste", "će", "ce", "ću", "cu", "ćemo", "cemo",
        "da", "li", "ne", "nije", "nisu", "nema", "imaju", "ima", "kao", "ili", "i",
        "za", "od", "do", "na", "u", "po", "sa", "se", "bez", "pri", "pre", "posle",
        "jos", "još", "vec", "već", "samo", "sve", "svi", "sva", "svo", "taj", "ta", "to",
        "ovaj", "ova", "ovo", "tako", "takođe", "takodje", "jer", "kad", "kada", "ako",
        "zasto", "zašto", "zato", "gde", "gdje", "moje", "moj", "moja", "tvoj", "tvoja",
        "informacije", "informacija", "sistem", "sistema", "sistemu", "student", "studenta",
    }
)


def _fold_sr(s: str) -> str:
    t = s.lower()
    for a, b in (("š", "s"), ("đ", "d"), ("č", "c"), ("ć", "c"), ("ž", "z")):
        t = t.replace(a, b)
    return t


def _extract_query_keywords(q: str) -> list[str]:
    """Značajne reči iz celog upita (za rerang), bez šumnih funkcijskih reči."""
    folded = _fold_sr(q)
    tokens = re.split(r"[^a-z0-9]+", folded)
    seen: dict[str, None] = {}
    for t in tokens:
        if len(t) < 3:
            continue
        if t in _STOPWORDS:
            continue
        seen.setdefault(t, None)
    return list(seen.keys())


def _extract_query_keywords_embed(q: str) -> list[str]:
    """Ključne reči za embedding bez ASCII-foldinga (Embedić: ošišana latinica ruši kvalitet)."""
    t = q.strip().lower()
    tokens = re.findall(r"\w+", t, flags=re.UNICODE)
    seen: dict[str, None] = {}
    for tok in tokens:
        if len(tok) < 3:
            continue
        if tok in _STOPWORDS or _fold_sr(tok) in _STOPWORDS:
            continue
        seen.setdefault(tok, None)
    return list(seen.keys())


def _expand_keyword_stems(kws: list[str]) -> list[str]:
    """Kratki morfološki pomoćnik (matematiku → matematik) za bolji embedding i rerang."""
    out: list[str] = []
    for w in kws:
        out.append(w)
        for n in (1, 2):
            if len(w) - n >= 4:
                out.append(w[:-n])
    return list(dict.fromkeys(out))


def _query_for_embedding(q: str) -> str:
    if not USE_QUERY_FOCUS:
        return q.strip()
    if USE_EMBED_PRESERVE_DIACRITICS:
        kws = _extract_query_keywords_embed(q)
    else:
        kws = _extract_query_keywords(q)
    if not kws:
        return q.strip()
    return " ".join(_expand_keyword_stems(kws))


def _keyword_matches_haystack(kw: str, hay: str) -> bool:
    if len(kw) < 3:
        return False
    if kw in hay:
        return True
    for n in (1, 2):
        if len(kw) - n >= 4 and kw[:-n] in hay:
            return True
    return False


def _payload_search_text(payload: dict[str, Any]) -> str:
    parts: list[str] = []
    for k in (
        "predmet_naziv",
        "predmet_sifra",
        "teme_kursa",
        "text",
        "cilj",
        "ishodi_ucenja",
        "metode_nastave",
    ):
        v = payload.get(k)
        if v:
            parts.append(str(v))
    return _fold_sr(" ".join(parts))


def _rerank_by_keyword_hits(
    original_query: str, rows: list[dict[str, Any]]
) -> list[dict[str, Any]]:
    if not rows or not USE_KEYWORD_RERANK:
        return rows
    kws = _extract_query_keywords(original_query)
    if not kws:
        return rows

    def sort_key(r: dict[str, Any]) -> tuple[int, float]:
        pl = r.get("payload")
        if not isinstance(pl, dict):
            pl = {}
        hay = _payload_search_text(pl)
        hits = sum(1 for kw in kws if _keyword_matches_haystack(kw, hay))
        base = float(r.get("score", 0.0))
        return (-hits, -base)

    return sorted(rows, key=sort_key)

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
        log.info(
            "Loading embedding model %s (Qdrant vectors dim=%d, preserve_sr_diacritics=%s)",
            EMBEDDING_MODEL,
            EMBED_DIM,
            USE_EMBED_PRESERVE_DIACRITICS,
        )
        _model = SentenceTransformer(EMBEDDING_MODEL)
    return _model


def collection_point_count() -> int | None:
    try:
        client = get_client()
        cols = {c.name for c in client.get_collections().collections}
        if COLLECTION not in cols:
            return 0
        info = client.get_collection(COLLECTION)
        return int(info.points_count)
    except Exception as e:
        log.warning("Could not read point count: %s", e)
        return None


@app.get("/health")
def health():
    try:
        get_client().get_collections()
        n = collection_point_count()
        body: dict[str, Any] = {
            "status": "ok",
            "qdrant": QDRANT_HOST,
            "collection": COLLECTION,
            "embedding_model": EMBEDDING_MODEL,
            "embed_dim": EMBED_DIM,
        }
        if n is not None:
            body["points"] = n
        return body
    except Exception as e:
        return {"status": "error", "detail": str(e)}


class SearchRequest(BaseModel):
    q: str = Field(..., min_length=1)
    limit: int = Field(12, ge=1, le=50)
    predmet_ids: list[int] | None = None


def _parse_predmet_ids(raw: str | None) -> list[int] | None:
    if not raw or not raw.strip():
        return None
    out: list[int] = []
    for part in raw.split(","):
        part = part.strip()
        if not part:
            continue
        try:
            out.append(int(part))
        except ValueError:
            continue
    return out or None


def _normalize_vector(v: Any) -> list[float] | None:
    if v is None:
        return None
    if isinstance(v, dict):
        if not v:
            return None
        v = next(iter(v.values()))
    if hasattr(v, "tolist"):
        v = v.tolist()
    try:
        return [float(x) for x in v]
    except (TypeError, ValueError):
        return None


def _prune_by_similarity(hits: list, limit: int) -> list:
    """Zadrži samo pogotke sa skorom blizu najboljeg (i iznad minimalnog praga)."""
    if not hits:
        return []
    ordered = sorted(hits, key=lambda h: -h.score)
    best = float(ordered[0].score)
    floor = max(MIN_VECTOR_SIM, best - SIM_GAP_FROM_BEST)
    kept = [h for h in ordered if float(h.score) >= floor]
    if not kept:
        kept = ordered[:1]
    if len(kept) < len(ordered):
        log.info(
            "Pruned %d weak vector hits (best=%.3f floor=%.3f)",
            len(ordered) - len(kept),
            best,
            floor,
        )
    return kept[:limit]


def _prune_scored_pairs(
    pairs: list[tuple[float, dict[str, Any]]], limit: int
) -> list[tuple[float, dict[str, Any]]]:
    if not pairs:
        return []
    pairs = sorted(pairs, key=lambda x: -x[0])
    best = float(pairs[0][0])
    floor = max(MIN_VECTOR_SIM, best - SIM_GAP_FROM_BEST)
    kept = [(s, pl) for s, pl in pairs if float(s) >= floor]
    if not kept:
        kept = pairs[:1]
    return kept[:limit]


def _cosine_sim(qv: list[float], pv: list[float]) -> float:
    if len(qv) != len(pv):
        return 0.0
    dot = sum(a * b for a, b in zip(qv, pv))
    nq = math.sqrt(sum(a * a for a in qv))
    npv = math.sqrt(sum(b * b for b in pv))
    return dot / (nq * npv + 1e-9)


def _run_search_scroll_program(q: str, limit: int, ids: list[int]) -> list[dict[str, Any]]:
    """Brute-force over all points: reliable when Qdrant search() behaves oddly."""
    model = get_model()
    q_vec = _query_for_embedding(q)
    enc = model.encode(q_vec)
    qvec = enc.tolist() if hasattr(enc, "tolist") else list(enc)
    qvec = [float(x) for x in qvec]
    client = get_client()
    allowed = set(ids)
    scored: list[tuple[float, dict[str, Any]]] = []
    offset = None
    while True:
        batch = client.scroll(
            collection_name=COLLECTION,
            offset=offset,
            limit=256,
            with_payload=True,
            with_vectors=True,
        )
        points, offset = batch
        for p in points:
            pl = p.payload
            if not isinstance(pl, dict):
                pl = dict(pl or {})
            pid = _payload_predmet_id(pl)
            if pid is None or pid not in allowed:
                continue
            pv = _normalize_vector(p.vector)
            if not pv:
                continue
            scored.append((_cosine_sim(qvec, pv), pl))
        if offset is None:
            break
    scored.sort(key=lambda x: -x[0])
    log.info("scroll search: %d candidates for %d allowed ids", len(scored), len(allowed))
    prune_cap = max(limit * 5, 20)
    scored = _prune_scored_pairs(scored, min(prune_cap, len(scored) or 1))
    rows = [{"score": float(s), "payload": pl} for s, pl in scored]
    rows = _rerank_by_keyword_hits(q, rows)
    return rows[:limit]


def _payload_predmet_id(payload: dict | None) -> int | None:
    if not payload or "predmet_id" not in payload:
        return None
    v = payload["predmet_id"]
    if isinstance(v, bool):
        return None
    if isinstance(v, int):
        return v
    if isinstance(v, float):
        return int(v)
    try:
        return int(str(v))
    except (TypeError, ValueError):
        return None


def _run_vector_search(q: str, limit: int, ids: list[int] | None):
    model = get_model()
    q_vec = _query_for_embedding(q)
    if USE_QUERY_FOCUS and q_vec != q.strip():
        log.info("Upit za embedding (fokus): %r", q_vec[:300])
    vec = model.encode(q_vec).tolist()
    client = get_client()
    prefetch = limit
    if ids:
        # Širi prefetch: inače filter po programu može da isprazni top-N globalnih pogodaka.
        prefetch = min(512, max(limit * 15, len(ids) * 8, 96))
    hits = client.search(
        collection_name=COLLECTION,
        query_vector=vec,
        query_filter=None,
        limit=prefetch,
        with_payload=True,
    )
    if ids:
        allowed = set(ids)
        before = len(hits)
        filtered = [
            h for h in hits
            if (pid := _payload_predmet_id(h.payload)) is not None and pid in allowed
        ]
        if before > 0 and len(filtered) == 0:
            sample = sorted(
                {p for h in hits if (p := _payload_predmet_id(h.payload)) is not None}
            )[:20]
            log.warning(
                "Post-filter removed all %d hits; allowed ids (first 15)=%s; payload predmet_id in batch=%s",
                before,
                ids[:15],
                sample,
            )
        hits = filtered
    prune_cap = max(limit * 5, 20)
    hits = _prune_by_similarity(hits, min(prune_cap, len(hits) or 1))
    out = [
        {"score": float(h.score), "payload": h.payload or {}} for h in hits
    ]
    out = _rerank_by_keyword_hits(q, out)
    out = out[:limit]
    if ids and not out:
        log.warning(
            "Vector search+filter prazan; scroll preko kolekcije (allowed ids=%d)",
            len(ids),
        )
        return _run_search_scroll_program(q, limit, ids)
    return out


@app.get("/api/v1/search")
def search_get(
    q: str = Query(..., min_length=1),
    limit: int = Query(12, ge=1, le=50),
    predmet_ids: str | None = Query(
        None,
        description="Comma-separated predmet IDs (PostgreSQL); restricts hits after vector search",
    ),
):
    ids = _parse_predmet_ids(predmet_ids)
    results = _run_vector_search(q, limit, ids)
    return {"query": q, "results": results}


@app.post("/api/v1/search")
def search_post(body: SearchRequest):
    """Preferred for long predmet_id lists (JSON body, no URL encoding issues)."""
    results = _run_vector_search(body.q, body.limit, body.predmet_ids)
    return {"query": body.q, "results": results}


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

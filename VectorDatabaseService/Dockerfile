FROM python:3.11-slim

WORKDIR /app
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# CPU-only PyTorch first — inače pip sa PyPI vuče torch+CUDA (~GB) i često pukne na hash/CND završetku.
RUN pip install --no-cache-dir --upgrade "pip>=25" \
    && pip install --no-cache-dir torch --index-url https://download.pytorch.org/whl/cpu

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY main.py ingest.py ./
COPY entrypoint.sh /entrypoint.sh
# Strip CRLF when the repo was checked out on Windows (fixes "no such file or directory" on exec)
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

EXPOSE 8000
HEALTHCHECK --interval=30s --timeout=15s --start-period=420s --retries=5 \
    CMD curl -sf http://localhost:8000/health || exit 1

ENTRYPOINT ["/entrypoint.sh"]

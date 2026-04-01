#!/bin/sh
set -e
if [ ! -s /data/chunks.jsonl ]; then
  echo "ERROR: /data/chunks.jsonl missing or empty (compose must mount scripts/vector -> /data)."
  exit 1
fi
echo "Running vector ingest..."
i=0
while [ "$i" -lt 6 ]; do
  if python ingest.py; then
    echo "Ingest OK, starting API."
    exec uvicorn main:app --host 0.0.0.0 --port 8000
  fi
  i=$((i + 1))
  echo "Ingest failed ($i/6), retry in 15s..."
  sleep 15
done
echo "Giving up: ingest never succeeded."
exit 1

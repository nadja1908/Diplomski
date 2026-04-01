#!/bin/sh
set -e
echo "Running vector ingest..."
python ingest.py || true
exec uvicorn main:app --host 0.0.0.0 --port 8000

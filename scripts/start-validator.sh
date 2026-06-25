#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"

cd "$PROJECT_DIR"

docker build -t zxcvbn-validator ./docker

docker rm -f zxcvbn-validator >/dev/null 2>&1 || true
docker run -d --name zxcvbn-validator -p 5000:5000 zxcvbn-validator

echo "Validator started on http://localhost:5000"

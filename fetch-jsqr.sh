#!/usr/bin/env bash
set -euo pipefail

URL="https://raw.githubusercontent.com/cozmo/jsQR/8e6a036beafa7053dd44b1b76ac578d22b1b3311/dist/jsQR.js"
EXPECTED_GIT_BLOB_SHA="99ea9df26907009e5553233ffe03c529c1521739"
OUT="${1:-jsQR.js}"

curl --fail --location --retry 3 --retry-delay 2 "$URL" --output "$OUT"
test -s "$OUT"
ACTUAL="$(git hash-object "$OUT")"
if [ "$ACTUAL" != "$EXPECTED_GIT_BLOB_SHA" ]; then
  echo "jsQR integrity check failed: expected $EXPECTED_GIT_BLOB_SHA, got $ACTUAL" >&2
  rm -f "$OUT"
  exit 1
fi
echo "Pinned jsQR ready: $OUT"

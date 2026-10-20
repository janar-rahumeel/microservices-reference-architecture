#!/usr/bin/env bash
# Exports a Keycloak realm (users and client secrets included) to config/keycloak/<realm>-realm.json.
# Keycloak must not be running during an export, so the keycloak service is stopped and restarted afterwards.
# Usage: ./export-keycloak-realm.sh [realm]   (default: mra)
set -euo pipefail

REALM="${1:-mra}"
cd "$(dirname "${BASH_SOURCE[0]}")"
OUT_DIR="config/keycloak"
OUT_FILE="${REALM}-realm.json"
HOST_OUT_DIR="$(cd "$OUT_DIR" && (pwd -W 2>/dev/null || pwd))"

WAS_RUNNING="$(docker compose ps --status running --services keycloak)"
restore() {
  if [ -n "$WAS_RUNNING" ]; then
    docker compose start keycloak
  fi
}
trap restore EXIT

# Never (re)create the DB here: that could swap its data volume. It only has to be running.
if [ -z "$(docker compose ps --status running --services keycloak-db)" ]; then
  echo "keycloak-db is not running" >&2
  exit 1
fi
docker compose stop keycloak

# Replaces the service command (start-dev --import-realm); DB settings are inherited from the compose service.
MSYS_NO_PATHCONV=1 docker compose run --rm --no-deps --user 0 \
  -v "${HOST_OUT_DIR}:/out" keycloak \
  export --realm "$REALM" --users same_file --file "/out/.${OUT_FILE}.tmp"

mv "${OUT_DIR}/.${OUT_FILE}.tmp" "${OUT_DIR}/${OUT_FILE}"
echo "Exported realm '${REALM}' to etc/docker/${OUT_DIR}/${OUT_FILE}"

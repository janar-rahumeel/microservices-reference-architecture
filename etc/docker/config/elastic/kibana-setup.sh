#!/bin/sh

set -eu

KIBANA_URL="http://kibana:5601"

echo "Creating MRA space ..."

if curl -fsS \
       -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
       "${KIBANA_URL}/api/spaces/space/mra" \
       >/dev/null 2>&1; then
    echo "MRA space already exists"
else
    curl -fsS \
        -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "kbn-xsrf: true" \
        "${KIBANA_URL}/api/spaces/space" \
        -d '{
          "id": "mra",
          "name": "MRA",
          "description": "Microservices Reference Architecture",
          "solution": "oblt",
          "initials": "M",
          "color": "#FFC9C2",
          "disabledFeatures": []
        }'
    echo
fi

echo
echo "Importing MRA saved objects ..."

curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    -X POST \
    -H "kbn-xsrf: true" \
    -F "file=@/config/saved-objects.ndjson" \
    "${KIBANA_URL}/s/mra/api/saved_objects/_import?overwrite=true"

echo
echo "Setting MRA default data view ..."

curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    -X POST \
    -H "Content-Type: application/json" \
    -H "kbn-xsrf: true" \
    "${KIBANA_URL}/s/mra/api/data_views/default" \
    -d '{
      "data_view_id": "mra-view",
      "force": true
    }'

echo
echo "Kibana configuration completed"
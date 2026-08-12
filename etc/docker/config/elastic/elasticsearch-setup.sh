#!/bin/sh

set -eu

ELASTICSEARCH_URL="http://elasticsearch:9200"

echo "Configuring kibana_system password ..."

curl -fsS \
  -u "${ELASTIC_USERNAME}:${ELASTIC_PASSWORD}" \
  -X POST \
  -H "Content-Type: application/json" \
  "${ELASTICSEARCH_URL}/_security/user/kibana_system/_password" \
  -d "{\"password\":\"${KIBANA_PASSWORD}\"}"

echo
echo "Creating MRA role ..."

curl -fsS \
  -u "${ELASTIC_USERNAME}:${ELASTIC_PASSWORD}" \
  -X PUT \
  -H "Content-Type: application/json" \
  "${ELASTICSEARCH_URL}/_security/role/mra_role" \
  -d '{
      "cluster": [],
      "indices": [
        {
          "names": ["logs-mra-*"],
          "privileges": [
            "read",
            "view_index_metadata"
          ]
        }
      ],
      "applications": [
        {
          "application": "kibana-.kibana",
          "privileges": [
            "feature_discover.read"
          ],
          "resources": [
            "space:mra"
          ]
        }
      ]
    }'

echo
echo "Creating MRA user ..."

curl -fsS \
  -u "${ELASTIC_USERNAME}:${ELASTIC_PASSWORD}" \
  -X PUT \
  -H "Content-Type: application/json" \
  "${ELASTICSEARCH_URL}/_security/user/${MRA_USERNAME}" \
  -d "{
    \"password\": \"${MRA_PASSWORD}\",
    \"roles\": [\"mra_role\"],
    \"full_name\": \"MRA\"
  }"

echo
echo "Creating MRA component template ..."

curl -fsS \
  -u "${ELASTIC_USERNAME}:${ELASTIC_PASSWORD}" \
  -X PUT \
  -H 'Content-Type: application/json' \
  "${ELASTICSEARCH_URL}/_component_template/mra@mappings" \
  --data-binary @/config/component-template.json

echo
echo "Creating MRA logs index template ..."

curl -fsS \
  -u "${ELASTIC_USERNAME}:${ELASTIC_PASSWORD}" \
  -X PUT \
  -H 'Content-Type: application/json' \
  "${ELASTICSEARCH_URL}/_index_template/mra-logs" \
  --data-binary @/config/index-template.json

echo
echo "Elasticsearch configuration completed"
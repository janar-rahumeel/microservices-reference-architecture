#!/bin/sh

set -eu

KIBANA_URL="http://kibana:5601"

FLEET_SERVER_POLICY_ID="fleet-server-policy"
FLEET_SERVER_PACKAGE_POLICY_ID="mra-fleet-server"
FLEET_SERVER_HOST="https://fleet.mra.local:8220"
DEFAULT_POLICY_ID="mra-default-policy"

echo
echo "Configuring Fleet Server URL ..."

if curl -fsS \
       -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
       "${KIBANA_URL}/api/fleet/fleet_server_hosts/mra-fleet-server" \
       >/dev/null 2>&1; then
    echo "Fleet Server URL already exists"
else
    curl -fsS \
        -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "kbn-xsrf: true" \
        "${KIBANA_URL}/api/fleet/fleet_server_hosts" \
        -d '{
          "id": "mra-fleet-server",
          "name": "MRA Fleet Server",
          "host_urls": [
            "'"${FLEET_SERVER_HOST}"'"
          ],
          "is_default": true
        }'
    echo
fi

echo
echo "Waiting for Fleet Elasticsearch output ..."

MAX_ATTEMPTS=60
ATTEMPT=1

while ! curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    "${KIBANA_URL}/api/fleet/outputs/fleet-default-output" \
    >/dev/null 2>&1
do
    if [ "${ATTEMPT}" -ge "${MAX_ATTEMPTS}" ]; then
        echo "ERROR: Fleet Elasticsearch output did not become available" >&2
        exit 1
    fi

    echo "Fleet output not ready yet, waiting... (${ATTEMPT}/${MAX_ATTEMPTS})"
    sleep 2
    ATTEMPT=$((ATTEMPT + 1))
done

echo "Fleet Elasticsearch output found, updating ..."

curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    -X PUT \
    -H "Content-Type: application/json" \
    -H "kbn-xsrf: true" \
    "${KIBANA_URL}/api/fleet/outputs/fleet-default-output" \
    -d '{
      "name": "default",
      "type": "elasticsearch",
      "hosts": [
        "http://elasticsearch:9200"
      ],
      "is_default": true,
      "is_default_monitoring": true,
      "preset": "balanced"
    }'

echo

echo
echo "Creating Fleet Server policy ..."

if curl -fsS \
       -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
       "${KIBANA_URL}/api/fleet/agent_policies/${FLEET_SERVER_POLICY_ID}" \
       >/dev/null 2>&1; then
    echo "Fleet Server policy already exists"
else
    curl -fsS \
        -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "kbn-xsrf: true" \
        "${KIBANA_URL}/api/fleet/agent_policies" \
        -d '{
          "id": "'"${FLEET_SERVER_POLICY_ID}"'",
          "name": "MRA Fleet Server",
          "description": "Fleet Server policy for MRA",
          "namespace": "default",
          "monitoring_enabled": [
            "logs",
            "metrics"
          ]
        }'
    echo
fi

echo
echo "Creating Fleet Server integration ..."

if curl -fsS \
       -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
       "${KIBANA_URL}/api/fleet/package_policies/${FLEET_SERVER_PACKAGE_POLICY_ID}" \
       >/dev/null 2>&1; then
    echo "Fleet Server integration already exists"
else
    curl -fsS \
        -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
        -X POST \
        -H "kbn-xsrf: true" \
        -H "Content-Type: application/json" \
        "${KIBANA_URL}/api/fleet/package_policies" \
        -d '{
          "id": "'"${FLEET_SERVER_PACKAGE_POLICY_ID}"'",
          "name": "MRA Fleet Server",
          "namespace": "default",
          "policy_id": "'"${FLEET_SERVER_POLICY_ID}"'",
          "package": {
            "name": "fleet_server",
            "version": "1.6.1"
          },
          "inputs": [
            {
              "type": "fleet-server",
              "policy_template": "fleet_server",
              "enabled": true,
              "streams": [],
              "vars": {}
            }
          ]
        }'
    echo
fi

echo
echo "Creating Fleet Server service token ..."

if [ -s "${FLEET_SERVER_SERVICE_TOKEN_PATH}" ]; then
    echo "Fleet Server service token already exists"
else
    curl -fsS \
        -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "kbn-xsrf: true" \
        "${KIBANA_URL}/api/fleet/service_tokens" \
        -d '{}' \
        | sed -n 's/.*"value":"\([^"]*\)".*/\1/p' \
        > "${FLEET_SERVER_SERVICE_TOKEN_PATH}"

    if [ ! -s "${FLEET_SERVER_SERVICE_TOKEN_PATH}" ]; then
        echo "ERROR: Fleet Server service token was not returned" >&2
        exit 1
    fi

    chown 1000:1000 "${FLEET_SERVER_SERVICE_TOKEN_PATH}"
    chmod 600 "${FLEET_SERVER_SERVICE_TOKEN_PATH}"

    echo "Fleet Server service token created"
fi

echo
echo "Creating MRA Default policy ..."

if curl -fsS \
       -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
       "${KIBANA_URL}/api/fleet/agent_policies/${DEFAULT_POLICY_ID}" \
       >/dev/null 2>&1; then
    echo "MRA Default policy already exists"
else
    curl -fsS \
        -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "kbn-xsrf: true" \
        "${KIBANA_URL}/api/fleet/agent_policies" \
        -d '{
          "id": "'"${DEFAULT_POLICY_ID}"'",
          "name": "MRA Default",
          "description": "Default MRA log collection policy",
          "namespace": "default",
          "monitoring_enabled": [
            "logs",
            "metrics"
          ]
        }'
    echo
fi

echo
echo "Creating MRA Agent enrollment token ..."

if [ -s "${MRA_AGENT_ENROLLMENT_TOKEN_PATH}" ]; then
    echo "MRA Agent enrollment token already exists"
else
    curl -sS \
        -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "kbn-xsrf: true" \
        "${KIBANA_URL}/api/fleet/enrollment_api_keys" \
        -d '{
          "name": "mra-data-collector",
          "policy_id": "'"${DEFAULT_POLICY_ID}"'"
        }' \
        | sed -n 's/.*"item":{.*"api_key":"\([^"]*\)".*/\1/p' \
        > "${MRA_AGENT_ENROLLMENT_TOKEN_PATH}"

    if [ ! -s "${MRA_AGENT_ENROLLMENT_TOKEN_PATH}" ]; then
        echo "ERROR: MRA Agent enrollment token was not returned" >&2
        exit 1
    fi

    chown 1000:1000 "${MRA_AGENT_ENROLLMENT_TOKEN_PATH}"
    chmod 600 "${MRA_AGENT_ENROLLMENT_TOKEN_PATH}"

    echo "MRA Agent enrollment token created"
fi

echo
echo "Creating Traefik Logs integration ..."

if curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    "${KIBANA_URL}/api/fleet/package_policies/mra-traefik-logs-policy" \
    >/dev/null 2>&1; then

    echo "Traefik Logs integration already exists"
else
  curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    -X POST \
    -H "Content-Type: application/json" \
    -H "kbn-xsrf: true" \
    "${KIBANA_URL}/api/fleet/package_policies" \
    -d '{
      "id": "mra-traefik-logs-policy",
      "name": "mra-traefik-logs",
      "description": "Traefik Logs",
      "namespace": "default",
      "policy_ids": [
        "'"${DEFAULT_POLICY_ID}"'"
      ],
      "enabled": true,
      "inputs": [
        {
          "type": "logfile",
          "policy_template": "traefik",
          "enabled": true,
          "streams": [
            {
              "enabled": true,
              "data_stream": {
                "type": "logs",
                "dataset": "traefik.access"
              },
              "vars": {
                "paths": {
                  "type": "text",
                  "value": [
                    "/var/lib/docker/containers/${docker.container.id}/*-json.log"
                  ]
                },
                "tags": {
                  "value": [
                    "forwarded"
                  ],
                  "type": "text"
                },
                "preserve_original_event": {
                  "value": false,
                  "type": "bool"
                },
                "processors": {
                  "type": "yaml",
                  "value": "- decode_json_fields:\n    fields:\n      - message\n    target: docker\n\n- script:\n    lang: javascript\n    source: |\n      function process(event) {\n        var log = event.Get(\"docker.log\");\n        if (log != null) {\n          event.Put(\"message\", log);\n        }\n      }\n\n- add_fields:\n    target: service\n    fields:\n      name: traefik"
                }
              }
            }
          ]
        }
      ],
      "package": {
        "name": "traefik",
        "title": "Traefik",
        "version": "2.7.0"
      },
      "condition": "${docker.container.name} == \"docker-traefik-1\"",
      "force": false,
      "create_dataset_templates": true
    }'
fi

echo
echo "Creating MRA Application Logs integration ..."

if curl -fsS \
   -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
   "${KIBANA_URL}/api/fleet/package_policies/mra-application-logs-policy" \
   >/dev/null 2>&1; then

   echo "MRA Application Logs integration already exists"
else
   curl -fsS \
       -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
       -X POST \
       -H "Content-Type: application/json" \
       -H "kbn-xsrf: true" \
       "${KIBANA_URL}/api/fleet/package_policies" \
       -d '{
         "id": "mra-application-logs-policy",
         "name": "mra-application-logs",
         "description": "MRA application logs",
         "namespace": "local",
         "policy_ids": [
           "'"${DEFAULT_POLICY_ID}"'"
         ],
         "enabled": true,
         "inputs": [
           {
             "type": "filestream",
             "policy_template": "filestream",
             "enabled": true,
             "streams": [
               {
                 "enabled": true,
                 "data_stream": {
                   "type": "logs",
                   "dataset": "filestream.filestream"
                 },
                 "vars": {
                   "paths": {
                     "type": "text",
                     "value": [
                       "/logs/*.log"
                     ]
                   },
                   "compression_gzip": {
                     "value": false,
                     "type": "bool"
                   },
                   "use_logs_stream": {
                     "value": false,
                     "type": "bool"
                   },
                   "data_stream.dataset": {
                     "type": "text",
                     "value": "mra.application"
                   },
                   "pipeline": {
                     "type": "text"
                   },
                   "parsers": {
                     "value": "- ndjson:\n    target: \"\"\n",
                     "type": "yaml"
                   },
                   "exclude_files": {
                     "value": [
                       "\\\\.gz$"
                     ],
                     "type": "text"
                   },
                   "include_files": {
                     "value": [],
                     "type": "text"
                   },
                   "processors": {
                     "type": "yaml"
                   },
                   "tags": {
                     "value": [],
                     "type": "text"
                   },
                   "encoding": {
                     "type": "text"
                   },
                   "recursive_glob": {
                     "value": true,
                     "type": "bool"
                   },
                   "symlinks": {
                     "type": "bool"
                   },
                   "resend_on_touch": {
                     "type": "bool"
                   },
                   "check_interval": {
                     "type": "text"
                   },
                   "ignore_older": {
                     "type": "text"
                   },
                   "ignore_inactive": {
                     "type": "text"
                   },
                   "close_on_state_changed_inactive": {
                     "type": "text"
                   },
                   "close_on_state_changed_renamed": {
                     "type": "bool"
                   },
                   "close_on_state_changed_removed": {
                     "type": "bool"
                   },
                   "close_reader_eof": {
                     "type": "bool"
                   },
                   "close_reader_after_interval": {
                     "type": "text"
                   },
                   "clean_inactive": {
                     "value": -1,
                     "type": "text"
                   },
                   "clean_removed": {
                     "value": true,
                     "type": "bool"
                   },
                   "harvester_limit": {
                     "value": 0,
                     "type": "integer"
                   },
                   "backoff_init": {
                     "type": "text"
                   },
                   "backoff_max": {
                     "type": "text"
                   },
                   "fingerprint": {
                     "value": true,
                     "type": "bool"
                   },
                   "fingerprint_offset": {
                     "value": 0,
                     "type": "integer"
                   },
                   "fingerprint_length": {
                     "value": 1024,
                     "type": "integer"
                   },
                   "file_identity_native": {
                     "value": false,
                     "type": "bool"
                   },
                   "rotation_external_strategy_copytruncate": {
                     "type": "yaml"
                   },
                   "exclude_lines": {
                     "value": [],
                     "type": "text"
                   },
                   "include_lines": {
                     "value": [],
                     "type": "text"
                   },
                   "buffer_size": {
                     "type": "text"
                   },
                   "message_max_bytes": {
                     "type": "text"
                   },
                   "condition": {
                     "type": "text"
                   },
                   "delete_enabled": {
                     "value": false,
                     "type": "bool"
                   },
                   "delete_grace_period": {
                     "type": "text"
                   },
                   "data_stream.type": {
                     "value": "logs",
                     "type": "text"
                   }
                 }
               }
             ]
           }
         ],
         "package": {
           "name": "filestream",
           "title": "Custom Logs (Filestream)",
           "version": "2.5.1"
         },
         "force": false,
         "create_dataset_templates": true
       }'

   echo
   echo "MRA Application Logs integration created"
fi

echo
echo "Fleet setup completed"
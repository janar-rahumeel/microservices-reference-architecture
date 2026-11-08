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
                  "value": "- decode_json_fields:\n    fields:\n      - message\n    target: docker_envelope\n    overwrite_keys: true\n\n- drop_fields:\n    fields:\n      - message\n\n- copy_fields:\n    fields:\n      - from: docker_envelope.log\n        to: message\n    fail_on_error: true\n    ignore_missing: false\n\n- drop_fields:\n    fields:\n      - docker_envelope\n\n- add_fields:\n    target: service\n    fields:\n      name: traefik"
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
      "condition": "${docker.container.labels.co.elastic.logs.enabled} == \"true\" and  ${docker.container.labels.co.elastic.logs.integration} == \"traefik\"",
      "force": false,
      "create_dataset_templates": true
    }'
fi

echo
echo "Creating Nginx Logs integration ..."

if curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    "${KIBANA_URL}/api/fleet/package_policies/mra-nginx-logs-policy" \
    >/dev/null 2>&1; then

    echo "Nginx Logs integration already exists"
else
  curl -fsS \
    -u "${KIBANA_USERNAME}:${KIBANA_PASSWORD}" \
    -X POST \
    -H "Content-Type: application/json" \
    -H "kbn-xsrf: true" \
    "${KIBANA_URL}/api/fleet/package_policies" \
    -d '{
      "id": "mra-nginx-logs-policy",
      "name": "mra-nginx-logs",
      "description": "MRA Nginx Logs",
      "namespace": "default",
      "policy_ids": [
        "'"${DEFAULT_POLICY_ID}"'"
      ],
      "enabled": true,
      "inputs": [
        {
          "type": "logfile",
          "policy_template": "nginx",
          "enabled": true,
          "streams": [
            {
              "enabled": true,
              "data_stream": {
                "type": "logs",
                "dataset": "nginx.access"
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
                    "nginx-access"
                  ],
                  "type": "text"
                },
                "preserve_original_event": {
                  "value": false,
                  "type": "bool"
                },
                "ignore_older": {
                  "value": "72h",
                  "type": "text"
                },
                "processors": {
                  "type": "yaml",
                  "value": "- decode_json_fields:\n    fields:\n      - message\n    process_array: false\n    max_depth: 1\n    target: docker\n    overwrite_keys: false\n    add_error_key: true\n\n- drop_event:\n    when:\n      not:\n        equals:\n          docker.stream: stdout\n\n- drop_fields:\n    fields:\n      - message\n    ignore_missing: true\n\n- rename:\n    fields:\n      - from: docker.log\n        to: message\n    ignore_missing: true\n    fail_on_error: false\n\n- drop_fields:\n    fields:\n      - docker.stream\n      - docker.time\n    ignore_missing: true\n\n- rename:\n    fields:\n      - from: container.labels.com_docker_compose_service\n        to: service.name\n    ignore_missing: true\n    fail_on_error: false"
                }
              }
            },
            {
              "enabled": true,
              "data_stream": {
                "type": "logs",
                "dataset": "nginx.error"
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
                    "nginx-error"
                  ],
                  "type": "text"
                },
                "preserve_original_event": {
                  "value": false,
                  "type": "bool"
                },
                "ignore_older": {
                  "value": "72h",
                  "type": "text"
                },
                "processors": {
                  "type": "yaml",
                  "value": "- decode_json_fields:\n    fields:\n      - message\n    process_array: false\n    max_depth: 1\n    target: docker\n    overwrite_keys: false\n    add_error_key: true\n\n- drop_event:\n    when:\n      not:\n        equals:\n          docker.stream: stderr\n\n- drop_fields:\n    fields:\n      - message\n    ignore_missing: true\n\n- rename:\n    fields:\n      - from: docker.log\n        to: message\n    ignore_missing: true\n    fail_on_error: false\n\n- drop_fields:\n    fields:\n      - docker.stream\n      - docker.time\n    ignore_missing: true\n\n- rename:\n    fields:\n      - from: container.labels.com_docker_compose_service\n        to: service.name\n    ignore_missing: true\n    fail_on_error: false"
                }
              }
            }
          ]
        }
      ],
      "package": {
        "name": "nginx",
        "title": "Nginx",
        "version": "3.2.2"
      },
      "condition": "${docker.container.labels.co.elastic.logs.enabled} == \"true\" and ${docker.container.labels.co.elastic.logs.integration} == \"nginx\"",
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
       "namespace": "",
       "policy_ids": [
         "'"${DEFAULT_POLICY_ID}"'"
       ],
       "enabled": true,
       "condition": "${docker.container.labels.co.elastic.logs.enabled} == \"true\" and ${docker.container.labels.co.elastic.logs.integration} == \"standard\"",
       "inputs": [
         {
           "type": "docker/metrics",
           "policy_template": "docker",
           "enabled": false,
           "streams": [
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.container"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.cpu"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.diskio"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.event"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.healthcheck"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.image"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.info"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.memory"
               }
             },
             {
               "enabled": false,
               "data_stream": {
                 "type": "metrics",
                 "dataset": "docker.network"
               }
             }
           ]
         },
         {
           "type": "filestream",
           "policy_template": "docker",
           "enabled": true,
           "streams": [
             {
               "enabled": true,
               "data_stream": {
                 "type": "logs",
                 "dataset": "docker.container_logs",
                 "elasticsearch": {
                   "dynamic_dataset": true,
                   "dynamic_namespace": true
                 }
               },
               "vars": {
                 "paths": {
                   "value": [
                     "/var/lib/docker/containers/${docker.container.id}/*-json.log"
                   ],
                   "type": "text"
                 },
                 "containerParserStream": {
                   "value": "all",
                   "type": "text"
                 },
                 "additionalParsersConfig": {
                   "type": "yaml",
                   "value": "- ndjson:\n    target: \"\"\n    add_error_key: true\n    ignore_decoding_error: true\n"
                 }
               }
             }
           ]
         }
       ],
       "package": {
         "name": "docker",
         "title": "Docker",
         "version": "2.16.0"
       },
       "force": false,
       "create_dataset_templates": true
     }'

   echo
   echo "MRA Application Logs integration created"
fi

echo
echo "Fleet setup completed"

deploy-keycloak-db:
	docker compose -f ./etc/docker/docker-compose.yml up keycloak-db -d

deploy-keycloak:
	docker compose -f ./etc/docker/docker-compose.yml up keycloak -d


deploy-keycloak-db:
	docker compose -f ./etc/docker/docker-compose.yaml up keycloak-db -d

deploy-keycloak:
	docker compose -f ./etc/docker/docker-compose.yaml up keycloak -d

deploy-rabbitmq:
	docker compose -f ./etc/docker/docker-compose.yaml up rabbitmq -d

deploy-blackbox:
	docker compose -f ./etc/docker/docker-compose.yaml up blackbox -d

deploy-prometheus:
	docker compose -f ./etc/docker/docker-compose.yaml up prometheus -d

deploy-tempo:
	docker compose -f ./etc/docker/docker-compose.yaml up tempo -d

deploy-grafana:
	docker compose -f ./etc/docker/docker-compose.yaml up grafana -d

deploy-elasticsearch-setup:
	docker compose -f ./etc/docker/docker-compose.yaml up elasticsearch-setup -d

deploy-elasticsearch:
	docker compose -f ./etc/docker/docker-compose.yaml up elasticsearch -d

deploy-kibana-setup:
	docker compose -f ./etc/docker/docker-compose.yaml up kibana-setup -d

deploy-kibana:
	docker compose -f ./etc/docker/docker-compose.yaml up kibana -d

deploy-elastic-agent:
	docker compose -f ./etc/docker/docker-compose.yaml up elastic-agent -d

start-ui:
	$(MAKE) -C ui start

start-gateway:
	mvnw -pl gateway -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-core:
	mvnw -pl core -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-worker:
	mvnw -pl worker -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-c4:
	docker compose -f ./etc/docker/docker-compose.yaml up likec4 -d

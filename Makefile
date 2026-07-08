
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

start-ui:
	$(MAKE) -C ui start

start-gateway:
	mvn -f gateway/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-core:
	mvn -f core/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-worker:
	mvn -f worker/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

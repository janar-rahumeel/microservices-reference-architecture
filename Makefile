
deploy-keycloak-db:
	docker compose -f ./etc/docker/docker-compose.yml up keycloak-db -d

deploy-keycloak:
	docker compose -f ./etc/docker/docker-compose.yml up keycloak -d

deploy-rabbitmq:
	docker compose -f ./etc/docker/docker-compose.yml up rabbitmq -d

deploy-tempo:
	docker compose -f ./etc/docker/docker-compose.yml up tempo -d

deploy-grafana:
	docker compose -f ./etc/docker/docker-compose.yml up grafana -d

start-ui:
	$(MAKE) -C ui start

start-gateway:
	mvn -f gateway/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-core:
	mvn -f core/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-worker:
	mvn -f worker/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

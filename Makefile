
deploy-keycloak-db:
	docker compose -f ./etc/docker/docker-compose.yml up keycloak-db -d

deploy-keycloak:
	docker compose -f ./etc/docker/docker-compose.yml up keycloak -d

start-ui:
	$(MAKE) -C ui start

start-gateway:
	mvn -f gateway/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-core:
	mvn -f core/pom.xml -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

APPLICATIONS := gateway core worker token
IMAGE_REGISTRY ?=
IMAGE_TAG ?= $(shell git rev-parse --short HEAD)
export MRA_IMAGE_REGISTRY = $(IMAGE_REGISTRY)
export MRA_IMAGE_TAG = $(IMAGE_TAG)

.PHONY: build-images deploy-applications

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

deploy-elasticsearch:
	docker compose -f ./etc/docker/docker-compose.yaml up elasticsearch -d

deploy-kibana:
	docker compose -f ./etc/docker/docker-compose.yaml up kibana -d

deploy-elastic-agent:
	docker compose -f ./etc/docker/docker-compose.yaml up elastic-agent -d

deploy-traefik:
	docker compose -f ./etc/docker/docker-compose.yaml up traefik -d

deploy-traefik-experimental:
	docker compose -f ./etc/docker/docker-compose.yaml up traefik-experimental -d

deploy-redis-experimental:
	docker compose -f ./etc/docker/docker-compose.yaml up redis-experimental -d

deploy-mra-db:
	docker compose -f ./etc/docker/docker-compose.yaml up mra-db -d

start-ui:
	$(MAKE) -C ui start

start-token-experimental:
	mvnw -pl token -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-gateway:
	mvnw -pl gateway -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-core:
	mvnw -pl core -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

start-worker:
	mvnw -pl worker -Dspring-boot.run.jvmArguments="-Dspring.profiles.active=local" spring-boot:run

package-applications:
	mvnw -pl gateway,core,worker,token -am package -DskipTests -Dplugin.spotless.skip=true

package-ui:
	mvnw -pl ui -am package -DskipTests

image-%:
	docker build \
	    -f etc/docker/java.Dockerfile \
		--build-arg MODULE=$* \
		--build-arg IMAGE_VERSION=$(IMAGE_TAG) \
		--build-arg IMAGE_REVISION=$(shell git rev-parse HEAD) \
		-t $(IMAGE_REGISTRY)mra/$*:$(IMAGE_TAG) \
		-t $(IMAGE_REGISTRY)mra/$*:latest \
		$*

image-ui:
	docker build \
		-f etc/docker/nginx.Dockerfile \
		-t $(IMAGE_REGISTRY)mra/ui:$(IMAGE_TAG) \
		-t $(IMAGE_REGISTRY)mra/ui:latest \
		.

build-images: package-applications package-ui $(addprefix image-,$(APPLICATIONS)) image-ui

deploy-applications:
	docker compose -f ./etc/docker/docker-compose.yaml up gateway core worker ui -d

deploy-token-experimental:
	docker compose -f ./etc/docker/docker-compose.yaml up token-experimental -d

start-c4:
	docker compose -f ./etc/docker/docker-compose.yaml up likec4 -d

reset:
	docker compose -f ./etc/docker/docker-compose.yaml down -v

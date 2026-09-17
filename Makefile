KEY ?= 00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff
GRADLE ?= mise exec gradle@9.7.0 -- mise exec java@temurin-25.0.4+7 -- gradle
CONFIG ?= pool.yaml
POOL ?= research-pool
FILES ?= files
HOST ?= 127.0.0.1
PORT ?= 9000
OUTPUT ?= received
CONTRIBUTION ?= 1073741824
LEASE_SECONDS ?= 86400
DOCKER_CLIENT_DIR ?= docker/client
DOCKER_POOL_DIR ?= docker/pool-data

.DEFAULT_GOAL := help
.PHONY: help test build init identity files upload start server pool-server client send list cli-files cli-upload docker-build docker-init docker-identity docker-pool docker-upload docker-down docker-transfer

help:
	@printf '%s\n' \
	  'make init' \
	  'make identity CONFIG=pool.yaml' \
	  'make list' \
	  'make upload FILE=./files/photo.bin' \
	  'make start' \
	  'make send HOST=127.0.0.1 PORT=9000 FILE=./photo.bin KEY=<key-hex>' \
	  'make test' \
	  'make docker-build KEY=<key-hex>' \
	  'make docker-init KEY=<key-hex>' \
	  'make docker-pool KEY=<key-hex>' \
	  'make docker-upload KEY=<key-hex>' \
	  'make docker-identity' \
	  'make docker-down' \
	  'make docker-transfer KEY=<key-hex>'

test:
	$(GRADLE) test

build:
	$(GRADLE) classes

init: build
	java -cp build/classes/java/main com.p2pft.client.P2PClientCli init $(CONFIG) $(POOL) $(FILES) $(HOST) $(PORT) $(KEY) $(CONTRIBUTION)

identity: build
	java -cp build/classes/java/main com.p2pft.client.P2PClientCli identity $(CONFIG)

files: build
	java -cp build/classes/java/main com.p2pft.client.P2PClientCli files $(CONFIG)

list: files

upload: build
	@test -n "$(FILE)" || (printf '%s\n' 'FILE is required' >&2; exit 2)
	java -cp build/classes/java/main com.p2pft.client.P2PClientCli upload $(CONFIG) $(FILE)

server: build
	java -cp build/classes/java/main com.p2pft.server.P2PServer $(PORT) $(OUTPUT) $(KEY)

pool-server: build
	java -cp build/classes/java/main com.p2pft.server.P2PServer --config $(CONFIG) $(LEASE_SECONDS)

start: pool-server

client: build
	@test -n "$(FILE)" || (printf '%s\n' 'FILE is required' >&2; exit 2)
	java -cp build/classes/java/main com.p2pft.client.P2PClient $(HOST) $(PORT) $(FILE) $(KEY)

send: client

cli-files: files

cli-upload: upload

docker-build:
	P2PFT_KEY=$(KEY) docker compose build

docker-init: docker-build
	mkdir -p $(DOCKER_CLIENT_DIR)/files $(DOCKER_POOL_DIR) docker/received
	docker run --rm -v "$(PWD)/$(DOCKER_CLIENT_DIR):/data" p2pft-client com.p2pft.client.P2PClientCli init /data/pool.yaml $(POOL) /data/files pool 19010 $(KEY) $(CONTRIBUTION)

docker-identity:
	docker run --rm -v "$(PWD)/$(DOCKER_CLIENT_DIR):/data:ro" p2pft-client com.p2pft.client.P2PClientCli identity /data/pool.yaml

docker-pool:
	P2PFT_KEY=$(KEY) docker compose up -d pool

docker-upload: docker-pool
	P2PFT_KEY=$(KEY) docker compose run --rm cli

docker-down:
	P2PFT_KEY=$(KEY) docker compose down

docker-transfer:
	P2PFT_KEY=$(KEY) docker compose run --rm client

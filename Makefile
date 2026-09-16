KEY ?= 00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff
GRADLE ?= mise exec gradle@9.7.0 -- mise exec java@temurin-25.0.4+7 -- gradle
CONFIG ?= pool.yaml
POOL ?= research-pool
FILES ?= files
HOST ?= 127.0.0.1
PORT ?= 9000
OUTPUT ?= received
CONTRIBUTION ?= 1073741824

.DEFAULT_GOAL := help
.PHONY: help test build init files upload server pool-server client cli-files cli-upload docker-build docker-transfer

help:
	@printf '%s\n' \
	  'make init CONFIG=pool.yaml POOL=research-pool FILES=files HOST=127.0.0.1 PORT=9000' \
	  'make files CONFIG=pool.yaml' \
	  'make upload CONFIG=pool.yaml FILE=./files/photo.bin' \
	  'make server PORT=9000 OUTPUT=received KEY=<key-hex>' \
	  'make pool-server CONFIG=pool.yaml LEASE_SECONDS=86400' \
	  'make client HOST=127.0.0.1 PORT=9000 FILE=./photo.bin KEY=<key-hex>' \
	  'make test' \
	  'make docker-build KEY=<key-hex>' \
	  'make docker-transfer KEY=<key-hex>'

test:
	$(GRADLE) test

build:
	$(GRADLE) classes

init: build
	java -cp build/classes/java/main com.p2pft.client.P2PClientCli init $(CONFIG) $(POOL) $(FILES) $(HOST) $(PORT) $(KEY) $(CONTRIBUTION)

files: build
	java -cp build/classes/java/main com.p2pft.client.P2PClientCli files $(CONFIG)

upload: build
	@test -n "$(FILE)" || (printf '%s\n' 'FILE is required' >&2; exit 2)
	java -cp build/classes/java/main com.p2pft.client.P2PClientCli upload $(CONFIG) $(FILE)

server: build
	java -cp build/classes/java/main com.p2pft.server.P2PServer $(PORT) $(OUTPUT) $(KEY)

pool-server: build
	java -cp build/classes/java/main com.p2pft.server.P2PServer --config $(CONFIG) $(LEASE_SECONDS)

client: build
	@test -n "$(FILE)" || (printf '%s\n' 'FILE is required' >&2; exit 2)
	java -cp build/classes/java/main com.p2pft.client.P2PClient $(HOST) $(PORT) $(FILE) $(KEY)

cli-files: files

cli-upload: upload

docker-build:
	P2PFT_KEY=$(KEY) docker compose build

docker-transfer:
	P2PFT_KEY=$(KEY) docker compose run --rm client

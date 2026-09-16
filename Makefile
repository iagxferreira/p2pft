KEY ?= 00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff
GRADLE ?= mise exec gradle@9.7.0 -- mise exec java@temurin-25.0.4+7 -- gradle

.PHONY: test build docker-build docker-transfer

test:
	$(GRADLE) test

build:
	$(GRADLE) classes

docker-build:
	P2PFT_KEY=$(KEY) docker compose build

docker-transfer:
	P2PFT_KEY=$(KEY) docker compose run --rm client

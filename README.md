# P2PFT

P2PFT is an educational peer-to-peer file transfer project in Java. It uses a small custom protocol rather than BitTorrent:

- Files are split into 64 KiB chunks.
- Each transfer starts with a versioned binary manifest containing the file digest.
- Every chunk is encrypted and authenticated with AES-256-GCM.
- TCP framing preserves message boundaries.
- Receivers verify the complete SHA-256 digest before committing the file.
- UDP broadcast discovery announces peers on the local network.
- `QuorumValidator` demonstrates strict-majority validation of peer digest reports.

This is a learning implementation, not production-grade secure file sharing. The current key model is a pre-shared 32-byte key. It does not yet authenticate peer identities, persist resumable transfers, or coordinate a multi-peer download.

## Requirements

- Java 25+
- Gradle (the repository is compatible with the mise-managed Gradle installation)

## Verify

```bash
mise exec gradle@9.7.0 -- mise exec java@temurin-25.0.4+7 -- gradle test
```

## Run

Generate a 32-byte key as hexadecimal, for example with `openssl rand -hex 32`.

Start the segregated server:

```bash
java -cp build/classes/java/main com.p2pft.server.P2PServer 9000 received <key-hex>
```

Send a file with the segregated client:

```bash
java -cp build/classes/java/main com.p2pft.client.P2PClient 127.0.0.1 9000 ./photo.bin <key-hex>
```

The receiver writes the file only after decryption, chunk ordering, size, and whole-file digest checks succeed.

## Container transfer test

Place a fixture at `docker/input/sample.bin`, then run the client and server in isolated containers:

```bash
export P2PFT_KEY="$(openssl rand -hex 32)"
mkdir -p docker/input
printf 'container transfer fixture\n' > docker/input/sample.bin
docker compose run --rm client
docker compose cp server:/data/received/sample.bin ./docker/received.bin
sha256sum docker/input/sample.bin docker/received.bin
```

The Docker build runs the complete unit test suite before producing the Java 25 runtime image.

## Protocol shape

```text
TCP stream
  framed manifest
  framed encrypted chunk 0
  framed encrypted chunk 1
  ...
  framed OK or REJECTED response
```

The associated data for each encrypted chunk binds it to the transfer ID and chunk index, so an authenticated chunk cannot be moved to another position or transfer.

## Roadmap

1. Peer identity and authenticated key exchange instead of a shared key.
2. Signed manifests and signed peer reports.
3. Multi-peer chunk sourcing with quorum-based acceptance per chunk.
4. Resumable transfers and bounded disk/resource quotas.
5. Discovery response validation and optional multicast/TCP fallback.

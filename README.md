# P2PFT

P2PFT is an educational peer-to-peer file transfer and decentralized storage prototype in Java. It uses a small custom protocol rather than BitTorrent and is operated through `p2pft.sh` or the Makefile.

The project explores a storage trade: a node contributes persistent disk space and stays online as a seed, then receives pool storage credit bounded by that contribution. Files are encrypted before entering the pool so storage nodes do not need the decryption key.

## Current Scope

- Files are split into 64 KiB chunks.
- Each transfer starts with a versioned binary manifest containing the file digest.
- Every chunk is encrypted and authenticated with AES-256-GCM.
- TCP framing preserves message boundaries.
- Receivers verify the complete SHA-256 digest before committing the file.
- UDP broadcast discovery announces peers on the local network.
- Every process generates a SHA-256 peer identity and includes it in discovery and transfer logs.
- Structured logs report discovery counts, peer connections, file splitting, blob progress, verification, and rejection.
- `QuorumValidator` demonstrates strict-majority validation of peer digest reports.

This is a learning implementation, not production-grade secure file sharing. The current TCP transfer still uses a pre-shared 32-byte key, while Ed25519 identities and signed pool primitives are available for the next protocol layer. It does not yet authenticate TCP connections, persist resumable transfers, or coordinate a multi-peer download.

## Decentralized Bucket Model

The pool primitives now support the intended storage boundary:

- Each node has an Ed25519 public/private identity and a generated peer hash.
- `PoolConfig` persists the pool name and key pair in a local YAML file. Treat this file like a password and never commit it.
- A peer proves possession of its private key by signing pool protocol messages.
- The client generates a random file key and encrypts every blob before sending it to a pool.
- `PoolBlobStore` stores ciphertext bytes only. It has no decryption API and cannot reconstruct plaintext without the client-held file key.
- A node declares `contributed_bytes` in its pool YAML and its blob store rejects ciphertext beyond that quota.
- `SeedPolicy` enforces the initial exchange rule: requested logical bytes cannot exceed contributed seed bytes.

For example, a node offering 1 GiB of persistent storage can request up to 1 GiB of logical pool storage. It must remain a seed for the encrypted blobs it stores; deleting or going offline would be a future health/lease violation, not a way to retain the storage credit.

This is not yet a complete decentralized bucket network: membership admission, replicated blob routing, seed leases/health checks, threshold recovery, and encrypted file-key delivery still need to be added. A pool node can store opaque encrypted blobs, but availability and recovery are not implemented by simply copying ciphertext to one node.

## Requirements

- Java 25+
- Gradle (the repository is compatible with the mise-managed Gradle installation)

## Main Commands

Use the project wrapper or Makefile as the normal interface. They build with Java 25 automatically through mise.

```bash
./p2pft.sh init pool.yaml research-pool ./files 127.0.0.1 9000 <key-hex> 1073741824
./p2pft.sh files pool.yaml
./p2pft.sh upload pool.yaml ./files/photo.bin
./p2pft.sh server 9000 received <key-hex>
./p2pft.sh client 127.0.0.1 9000 ./photo.bin <key-hex>
./p2pft.sh pool-server pool.yaml 86400
```

Equivalent Makefile commands are available with `make help`, for example `make files CONFIG=pool.yaml` and `make upload CONFIG=pool.yaml FILE=./files/photo.bin`.

## Seed Trade

To join as a pool seed, launch `pool-server` with a config that declares a positive `contributed_bytes` value. The server announces its stable peer ID and SSH-style public-key fingerprint, then logs the lease expiration:

```text
event=seed_started peer=<peer-id> fingerprint=SHA256:<fingerprint> contributed_bytes=1073741824 lease_expires=...
```

The node is expected to stay online while its lease is active. If its lease expires or its server disappears, the pool may revoke its storage credit and mark its stored data for deletion. The current prototype records and exposes this policy but does not automatically delete data, because deletion is safe only after replication and seed health checks exist.

## Verify

```bash
mise exec gradle@9.7.0 -- mise exec java@temurin-25.0.4+7 -- gradle test
```

The wrapper command is:

```bash
./p2pft.sh test
```

The equivalent Makefile command is `make test`.

## Client CLI

Initialize one config per client. The command generates the Ed25519 identity and writes the private key to the YAML file; keep that file private.

```bash
./p2pft.sh init \
  pool.yaml research-pool ./files 127.0.0.1 9000 <key-hex> 1073741824
```

List files in the configured client directory:

```bash
./p2pft.sh files pool.yaml
```

Upload a file using the endpoint, transfer key, and peer identity from the same config:

```bash
./p2pft.sh upload pool.yaml ./files/photo.bin
```

The current `files` command lists the local client directory. A remote pool catalog and multi-node upload routing are separate protocol work; the current upload command uses the existing authenticated TCP prototype.

Logs use stable `key=value` fields, for example:

```text
event=file_split file=photo.bin blobs=4 chunk_size=65536
event=blob_sent peer=... blob=2 total=4
event=file_verified file=photo.bin sha256=...
```

Discovery announcements contain the peer hash and TCP port. Call `Discovery.listenForPeers` from a coordinator or monitoring process to collect announcements and emit the number of peers found.

## Run

Generate a 32-byte key as hexadecimal, for example with `openssl rand -hex 32`.

Start the segregated server:

```bash
./p2pft.sh server 9000 received <key-hex>
```

Send a file with the segregated client:

```bash
./p2pft.sh client 127.0.0.1 9000 ./photo.bin <key-hex>
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

1. Authenticate TCP connections with the configured Ed25519 identities.
2. Route encrypted blobs to multiple pool seeds instead of one TCP receiver.
3. Add signed manifests, signed peer reports, and quorum acceptance per blob.
4. Add replication, seed health checks, and lease-based safe deletion.
5. Add resumable transfers, encrypted file-key delivery, and threshold recovery.

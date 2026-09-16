# Contributor Guide

## Project Layout

- `src/main/java/com/p2pft/protocol`: shared framing, manifests, chunking, encryption, hashing, discovery, and quorum logic.
- `src/main/java/com/p2pft/client`: client executable and outbound transfer flow.
- `src/main/java/com/p2pft/server`: server executable and inbound transfer flow.
- `src/test/java`: behavior tests for protocol primitives.

## Development Rules

- Use Java 25 language and runtime features.
- Keep client and server responsibilities separated; shared behavior belongs in `protocol`.
- Write a failing test before adding or changing protocol behavior.
- Never write a received file before integrity validation succeeds.
- Treat wire-format changes as versioned protocol changes.
- Keep commits atomic and semantic: one behavior or coherent structural change per commit, with messages such as `feat: add authenticated chunk transfer` or `test: cover quorum validation`.

## Verification

Run `mise exec -- gradle test` before submitting changes. For changes to the TCP path, also run a local server/client transfer and confirm the output digest matches the input digest.

## Security Boundaries

The current prototype uses a pre-shared AES-256 key and does not provide peer identity authentication. Do not describe it as production-secure until key exchange, identity verification, replay protection, and resource limits are implemented.

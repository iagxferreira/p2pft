#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$root_dir"

usage() {
  printf '%s\n' \
    'Usage:' \
    '  ./p2pft.sh init [config]' \
    '  ./p2pft.sh identity [config]' \
    '  ./p2pft.sh list [config]' \
    '  ./p2pft.sh upload <file> [config]' \
    '  ./p2pft.sh start [config] [lease-seconds]' \
    '  ./p2pft.sh send <host> <port> <file> <key-hex>' \
    '  ./p2pft.sh init <config> <pool> <files-dir> <host> <port> <key-hex> <contributed-bytes>' \
    '  ./p2pft.sh test | build | docker-build | docker-transfer'
}

command="${1:-help}"
shift || true

case "$command" in
  init)
    if [[ $# -eq 0 || $# -eq 1 ]]; then
      config="${1:-pool.yaml}"
      key="${P2PFT_KEY:-$(openssl rand -hex 32)}"
      make init CONFIG="$config" POOL="${P2PFT_POOL:-research-pool}" FILES="${P2PFT_FILES:-files}" \
        HOST="${P2PFT_HOST:-127.0.0.1}" PORT="${P2PFT_PORT:-9000}" KEY="$key" \
        CONTRIBUTION="${P2PFT_CONTRIBUTION:-1073741824}"
    elif [[ $# -eq 7 ]]; then
      make init CONFIG="$1" POOL="$2" FILES="$3" HOST="$4" PORT="$5" KEY="$6" CONTRIBUTION="$7"
    else
      usage; exit 2
    fi
    ;;
  identity)
    [[ $# -le 1 ]] || { usage; exit 2; }
    make build >/dev/null
    java -cp build/classes/java/main com.p2pft.client.P2PClientCli identity "${1:-pool.yaml}"
    ;;
  files|list)
    [[ $# -le 1 ]] || { usage; exit 2; }
    make list CONFIG="${1:-pool.yaml}"
    ;;
  upload)
    [[ $# -ge 1 && $# -le 2 ]] || { usage; exit 2; }
    if [[ $# -eq 1 ]]; then make upload CONFIG=pool.yaml FILE="$1"; else make upload CONFIG="$2" FILE="$1"; fi
    ;;
  start|pool-server)
    [[ $# -le 2 ]] || { usage; exit 2; }
    make start CONFIG="${1:-pool.yaml}" LEASE_SECONDS="${2:-86400}"
    ;;
  server)
    [[ $# -eq 3 ]] || { usage; exit 2; }
    make server PORT="$1" OUTPUT="$2" KEY="$3"
    ;;
  send|client)
    [[ $# -eq 4 ]] || { usage; exit 2; }
    make client HOST="$1" PORT="$2" FILE="$3" KEY="$4"
    ;;
  test|build|docker-build|docker-transfer)
    [[ $# -eq 0 ]] || { usage; exit 2; }
    make "$command"
    ;;
  help|-h|--help)
    usage
    ;;
  *)
    usage
    exit 2
    ;;
esac

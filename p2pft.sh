#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$root_dir"

usage() {
  printf '%s\n' \
    'Usage:' \
    '  ./p2pft.sh init <config> <pool> <files-dir> <host> <port> <key-hex> <contributed-bytes>' \
    '  ./p2pft.sh files <config>' \
    '  ./p2pft.sh upload <config> <file>' \
    '  ./p2pft.sh server <port> <output-dir> <key-hex>' \
    '  ./p2pft.sh client <host> <port> <file> <key-hex>' \
    '  ./p2pft.sh test | build | docker-build | docker-transfer'
}

command="${1:-help}"
shift || true

case "$command" in
  init)
    [[ $# -eq 7 ]] || { usage; exit 2; }
    make init CONFIG="$1" POOL="$2" FILES="$3" HOST="$4" PORT="$5" KEY="$6" CONTRIBUTION="$7"
    ;;
  files)
    [[ $# -eq 1 ]] || { usage; exit 2; }
    make files CONFIG="$1"
    ;;
  upload)
    [[ $# -eq 2 ]] || { usage; exit 2; }
    make upload CONFIG="$1" FILE="$2"
    ;;
  server)
    [[ $# -eq 3 ]] || { usage; exit 2; }
    make server PORT="$1" OUTPUT="$2" KEY="$3"
    ;;
  client)
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

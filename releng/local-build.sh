#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

. "${SCRIPT_WD}/docker-build.sh"

. "${SCRIPT_WD}/docker-run.sh" "releng/build.sh"

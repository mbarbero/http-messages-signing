#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

export PASSWORD_STORE_DIR=${SCRIPT_WD}/secret-store

. "${SCRIPT_WD}/docker-build.sh"

export SONAR_ORG="$(pass sonar/org)"
export SONAR_TOKEN="$(pass sonar/token)"

. "${SCRIPT_WD}/docker-run.sh" "releng/build.sh"

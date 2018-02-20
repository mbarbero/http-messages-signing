#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

export PASSWORD_STORE_DIR=${SCRIPT_WD}/secret-store

export DOCKERHUB_USERNAME="$(pass dockerhub/username)"
export DOCKERHUB_PASSWORD="$(pass dockerhub/password)"

. "${SCRIPT_WD}/docker-deploy.sh"

export GPG_ENCRYPTION_PASSPHRASE="$(pass gpg/encryption-passphrase)"
export GPG_PASSPHRASE="$(pass gpg/passphrase)"

export SONATYPE_USERNAME="$(pass ossrh/username)"
export SONATYPE_PASSWORD="$(pass ossrh/password)"

. "${SCRIPT_WD}/docker-run.sh" "releng/deploy.sh"

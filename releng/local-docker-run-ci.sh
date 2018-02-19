#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")

export PASSWORD_STORE_DIR=${SCRIPT_DIR}/secret-store

export ENCRYPTION_PASSPHRASE="$(pass gpg-keyring-encryption-passphrase)"
export GPG_PASSPHRASE="$(pass signing-key-passphrase)"
export SONATYPE_USERNAME="$(pass ossrh/username)"
export SONATYPE_PASSWORD="$(pass ossrh/password)"

. "${SCRIPT_DIR}/docker-build.sh"

. "${SCRIPT_DIR}/docker-run-ci.sh"
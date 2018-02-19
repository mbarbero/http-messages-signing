#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
SRC="$(readlink -f "${SCRIPT_DIR}/..")"
SRC_MOUNTPOINT="/src"

POM_VERSION=$(cat pom.xml | grep "<version>" | sed '2,$d' | sed -E 's#.*<version>(.*)</version>#\1#')

docker run -it --rm \
	-e "ENCRYPTION_PASSPHRASE=${ENCRYPTION_PASSPHRASE}" \
	-e "GPG_PASSPHRASE=${GPG_PASSPHRASE}" \
	-e "SONATYPE_USERNAME=${SONATYPE_USERNAME}" \
	-e "SONATYPE_PASSWORD=${SONATYPE_PASSWORD}" \
	-v ${SRC}:${SRC_MOUNTPOINT} \
	mbarbero/http-messages-signing:${POM_VERSION} \
	"${SRC_MOUNTPOINT}/releng/ci.sh" "${SRC_MOUNTPOINT}"

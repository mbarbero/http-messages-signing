#!/usr/bin/env bash

set -o errexit
set -o nounset
#set -o pipefail

IFS=$'\n\t'

SRC_MOUNTPOINT="/src"

POM_VERSION=$(cat pom.xml | grep "<version>" | head -n1 | sed -E 's#.*<version>(.*)</version>#\1#')

docker run -it --rm \
	-e "ENCRYPTION_PASSPHRASE=${ENCRYPTION_PASSPHRASE}" \
	-e "GPG_PASSPHRASE=${GPG_PASSPHRASE}" \
	-e "GPG_HOME=${SRC_MOUNTPOINT}/releng/gnupg" \
	-e "SONATYPE_USERNAME=${SONATYPE_USERNAME}" \
	-e "SONATYPE_PASSWORD=${SONATYPE_PASSWORD}" \
	-v $(pwd):/${SRC_MOUNTPOINT} \
	mbarbero/http-messages-signing:${POM_VERSION} \
	"${SRC_MOUNTPOINT}/releng/ci.sh" "${SRC_MOUNTPOINT}"

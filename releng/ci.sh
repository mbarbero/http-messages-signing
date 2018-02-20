#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_PATH=$(dirname "$(readlink -f "$0")")

mvn -e -C -U -V -B clean verify -Pcompile -f "${SCRIPT_PATH}/../pom.xml"

export GPG_HOME="${SCRIPT_PATH}/gnupg"

openssl aes-256-cbc -pass pass:${ENCRYPTION_PASSPHRASE} -d -a \
	-in ${GPG_HOME}/private-keys-v1.d/F87B14B93BCEF6259480B139A63A13E97B306101.key.enc \
	-out ${GPG_HOME}/private-keys-v1.d/F87B14B93BCEF6259480B139A63A13E97B306101.key

mvn -e -C -U -V -B --settings ${SCRIPT_PATH}/maven/settings.xml clean deploy -Pprepare-deploy -Pdeploy -f "${SCRIPT_PATH}/../pom.xml"

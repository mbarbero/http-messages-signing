#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

export GPG_HOME="${SCRIPT_WD}/gnupg"
openssl aes-256-cbc -pass pass:${GPG_ENCRYPTION_PASSPHRASE} -d -a \
	-in ${GPG_HOME}/private-keys-v1.d/F87B14B93BCEF6259480B139A63A13E97B306101.key.enc \
	-out ${GPG_HOME}/private-keys-v1.d/F87B14B93BCEF6259480B139A63A13E97B306101.key

MAVEN_REPO=${MAVEN_REPO:-${HOME}/.m2/repository}
mvn -e -C -U -V -B -Dmaven.repo.local=${MAVEN_REPO} --settings ${SCRIPT_WD}/maven/settings.xml deploy -Pprepare-deploy -Pdeploy -f "${SCRIPT_WD}/../pom.xml"

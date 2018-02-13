#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

wd="${1:-pwd}"
cd "${wd}"

mvn -e -C -U -V -B clean verify -Pcompile

openssl aes-256-cbc -pass pass:${ENCRYPTION_PASSPHRASE} \
	-in ${GPG_HOME}/private-keys-v1.d/F87B14B93BCEF6259480B139A63A13E97B306101.key.enc \
	-out ${GPG_HOME}/private-keys-v1.d/F87B14B93BCEF6259480B139A63A13E97B306101.key -d

mvn -e -C -U -V -B --settings releng/maven/settings.xml clean deploy -Prelease

#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

# Build docker image which will be used as the build environment later
POM_VERSION=$(cat ${SCRIPT_WD}/../pom.xml | grep "<version>" | sed '2,$d' | sed -E 's#.*<version>(.*)</version>#\1#')
docker build --tag mbarbero/http-messages-signing:${POM_VERSION} releng/env

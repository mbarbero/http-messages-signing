#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

echo "${DOCKERHUB_PASSWORD}" | docker login --password-stdin --username "${DOCKERHUB_USERNAME}"
POM_VERSION=$(cat ${SCRIPT_WD}/../pom.xml | grep "<version>" | sed '2,$d' | sed -E 's#.*<version>(.*)</version>#\1#')
docker push mbarbero/http-messages-signing:${POM_VERSION}

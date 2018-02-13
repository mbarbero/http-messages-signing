#!/usr/bin/env bash

set -o errexit
set -o nounset
#set -o pipefail

IFS=$'\n\t'

POM_VERSION=$(cat pom.xml | grep "<version>" | head -n1 | sed -E 's#.*<version>(.*)</version>#\1#')
docker build --tag mbarbero/http-messages-signing:${POM_VERSION} releng/env
docker push mbarbero/http-messages-signing

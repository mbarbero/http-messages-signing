#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

MAVEN_REPO=${MAVEN_REPO:-${HOME}/.m2/repository}

mvn -e -C -U -V -B \
	-Dmaven.repo.local=${MAVEN_REPO} \
	clean verify -Pcompile -f "${SCRIPT_WD}/../pom.xml"

mvn -e -C -U -V -B \
	-Dmaven.repo.local=${MAVEN_REPO} \
	clean verify -Pprepare-deploy -f "${SCRIPT_WD}/../pom.xml"

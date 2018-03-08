#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")
SRC="$(readlink -f "${SCRIPT_WD}/..")"
MAVEN_REPO=${MAVEN_REPO:-${HOME}/.m2/repository}
MAVEN_REPO_MOUNTPOINT="/maven"

SRC_MOUNTPOINT="/src"

POM_VERSION=$(cat pom.xml | grep "<version>" | sed '2,$d' | sed -E 's#.*<version>(.*)</version>#\1#')

docker run -it --rm \
	-e "GPG_ENCRYPTION_PASSPHRASE=${GPG_ENCRYPTION_PASSPHRASE:-}" \
	-e "GPG_PASSPHRASE=${GPG_PASSPHRASE:-}" \
	-e "SONATYPE_USERNAME=${SONATYPE_USERNAME:-}" \
	-e "SONATYPE_PASSWORD=${SONATYPE_PASSWORD:-}" \
	-e "DOCKERHUB_USERNAME=${DOCKERHUB_USERNAME:-}" \
	-e "DOCKERHUB_PASSWORD=${DOCKERHUB_PASSWORD:-}" \
	-e "SONAR_TOKEN=${SONAR_TOKEN:-}" \
	-e "SONAR_ORG=${SONAR_ORG:-}" \
	-e "MAVEN_REPO=${MAVEN_REPO_MOUNTPOINT}" \
	-v ${SRC}:${SRC_MOUNTPOINT} \
	-v ${MAVEN_REPO}:${MAVEN_REPO_MOUNTPOINT} \
	mbarbero/http-messages-signing:${POM_VERSION} \
	"${SRC_MOUNTPOINT}/${1}"

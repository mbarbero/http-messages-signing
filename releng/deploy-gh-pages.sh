#!/usr/bin/env bash

set -o errexit
set -o nounset
set -o pipefail

IFS=$'\n\t'

SCRIPT_WD=$(dirname "$(readlink -f "$0")")

TMP_FOLDER=$(mktemp -d)
CREDENTIALS=$(mktemp)
echo "https://mbarbero:${GITHUB_TOKEN}@github.com" > ${CREDENTIALS}

git clone https://github.com/mbarbero/http-messages-signing.git --quiet --depth 2 --branch gh-pages --single-branch "${TMP_FOLDER}"
cd "${TMP_FOLDER}"

git config credential.helper "store --file ${CREDENTIALS}"

git config --add user.name "Mikael Barbero"
git config --add user.email "mikael.barbero@eclipse-foundation.org"

cp -rf ${SCRIPT_WD}/../target/site/apidocs/* ./apidocs/

git add --all
git commit -m "Deploy apidocs to github pages"
git push origin gh-pages

rm -f "${CREDENTIALS}"
rm -rf "${TMP_FOLDER}"
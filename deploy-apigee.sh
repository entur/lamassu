#!/bin/bash

set -e

ORG=entur
NAME=lamassu

function deploy {
    ENV="${1:-dev}"

    if ! [[ "$ENV" =~ ^(dev|staging|prod)$ ]]; then
        echo -e " 🙈 Invalid ENV: $ENV\n"
        exit 1
    fi

    read -rp " 😺 Apigee user: " APIGEEUSER
    read -rsp " 🔑 Apigee password: " APIGEEPASSWORD
    echo

    if ! command_exists jq; then
        brew install jq
    fi

    if [[ "$ENV" == "dev" ]]; then
        echo " 📝 Deploying new revision to Apigee dev ..."
        apigeetool deployproxy -V -o $ORG -e dev -n $NAME -d api/lamassu -u $APIGEEUSER -p $APIGEEPASSWORD
    fi

    APIGEEREVISION=$(apigeetool listdeployments -u $APIGEEUSER -p $APIGEEPASSWORD -o $ORG -n $NAME -j | jq '.deployments[] | select(.environment |contains("dev")) |.revision')

    if [[ "$ENV" == "staging" ]]; then
        echo " 📝 Deploying revision $APIGEEREVISION to Apigee stage ..."
        apigeetool deployExistingRevision -V -u $APIGEEUSER -p $APIGEEPASSWORD -o $ORG -e stage -n $NAME -r $APIGEEREVISION
    elif [[ "$ENV" == "prod" ]]; then
        echo " 📝 Deploying revision $APIGEEREVISION to Apigee prod ..."
        apigeetool deployExistingRevision -V -u $APIGEEUSER -p $APIGEEPASSWORD -o $ORG -e prod -n $NAME -r $APIGEEREVISION
    fi

    echo -e "\n 🎉 Revision $APIGEEREVISION successfully deployed to $ENV!"
    echo -e "\n 📋 Status: https://apigee.com/platform/$ORG/proxies/$NAME/overview/$APIGEEREVISION"
}

function command_exists {
    command -v $1 >/dev/null 2>&1;
}

ENV_ARGS="${@:-dev}"
for ENV_ARG in $ENV_ARGS
do
    deploy "$ENV_ARG"
done

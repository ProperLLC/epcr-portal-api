#!/bin/bash

set +e

if [ "$#" -ne 2 ]
then
    echo "Usage: deploy.sh -e [ staging|prod ]"
    exit 1
fi

DEPLOY_ENV="NOT_SET"

while getopts "e:" opt; do
    case $opt in
        e)
            DEPLOY_ENV=$OPTARG
            ;;
        \?)
            echo "Invalid option: -$OPTARG" >&2
            exit 1
            ;;
        :)
            echo "Option -$OPTARG requires an argument." >&2
            exit 1
            ;;
    esac
done

if [ DEPLOY_ENV -ne "staging" ] && [ DEPLOY_ENV -ne "prod" ]
then
    echo "Invalid deployment environment!  Must be either 'staging' or 'prod'"
    exit 1
fi

echo "Deployment Environment: $DEPLOY_ENV"

PROJECT_HOME=/Users/terry/projects/properllc/epcr-portal-api
OPENSHIFT_HOME=/Users/terry/projects/openshift/$DEPLOY_ENV/api
DIST_FILENAME=epcr-portal-api-1.1-SNAPSHOT

echo "Here we go..."

cd $PROJECT_HOME

play clean compile dist

cp target/universal/$DIST_FILENAME.zip $OPENSHIFT_HOME

cd $OPENSHIFT_HOME
rm -rf server
unzip $DIST_FILENAME.zip
mv $DIST_FILENAME server
rm $DIST_FILENAME.zip

git add .
git commit -m 'updated code - automated build process'
git push

echo "fini"
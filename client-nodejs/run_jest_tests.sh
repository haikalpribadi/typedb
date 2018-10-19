#!/usr/bin/env bash

set -x
ORIG_DIR=$(pwd)

echo "Preparing module for testing"
cd client-nodejs/ &> /dev/null
tar xvf grakn_dummy_binary_deploy.tar.gz &> /dev/null
cd grakn_dummy_binary_bundle/grakn_dummy_binary_files/ &> /dev/null
mv node_modules/client-nodejs/grakn/ . &> /dev/null
cd grakn &> /dev/null

echo "Running test suite"
../node ../node_modules/jest/bin/jest.js || exit 1

echo "Cleaning"
cd $ORIG_DIR &> /dev/null
rm -rf ./grakn_dummy_binary_bundle/ &> /dev/null

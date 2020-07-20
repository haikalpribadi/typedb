#!/usr/bin/env bash

./dist/grakn-core-all-linux/grakn server stop
rm -rf dist && mkdir dist
bazel build //:assemble-linux-targz
tar -xf bazel-bin/grakn-core-all-linux.tar.gz -C ./dist
./dist/grakn-core-all-linux/grakn server start
#!/usr/bin/env bash

sed -i.bak -e 's@protocol/session/@@g' $1
mv $1 $2

#!/bin/bash

curl -sSLO https://github.com/pinterest/ktlint/releases/download/1.5.0/ktlint
chmod a+x ktlint
$(pwd)/ktlint --format

#!/bin/bash

curl -sSLO https://github.com/pinterest/ktlint/releases/download/0.49.1/ktlint
chmod a+x ktlint
$(pwd)/ktlint --format

#!/bin/bash

export PHANTOM_PATH=/path/to/phantomjs-x.y.z-macosx/bin/phantomjs
export CRISON_WIDTH=400
export CRISON_HEIGHT=800
java -jar crison-standalone.jar "$@"

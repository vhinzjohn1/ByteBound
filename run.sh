#!/bin/sh
set -eu

cd "$(dirname "$0")"
mkdir -p bin
javac -encoding UTF-8 -d bin -sourcepath src \
    -cp "lib/jlayer-1.0.1.jar" src/*.java
exec java -cp "bin:src:lib/jlayer-1.0.1.jar" ByteBound

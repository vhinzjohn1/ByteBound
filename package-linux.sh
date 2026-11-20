#!/bin/sh
set -eu

if [ "$(uname -s)" != "Linux" ] || [ "$(uname -m)" != "x86_64" ]; then
    echo "This packaging script builds Linux x86_64 only." >&2
    exit 1
fi

cd "$(dirname "$0")"
build_dir=$(mktemp -d "${TMPDIR:-/tmp}/bytebound-package.XXXXXX")
trap 'rm -rf "$build_dir"' EXIT HUP INT TERM

mkdir -p "$build_dir/classes" "$build_dir/input" "$build_dir/package"

javac --release 11 -encoding UTF-8 \
    -d "$build_dir/classes" \
    -sourcepath src \
    -cp lib/jlayer-1.0.1.jar \
    src/*.java

cp -R src/charSprites src/icon src/music src/portalSprites "$build_dir/classes/"
cp "src/titlescreen bg.jpg" "$build_dir/classes/"

cat > "$build_dir/manifest.mf" <<'EOF'
Manifest-Version: 1.0
Main-Class: ByteBound
Class-Path: jlayer-1.0.1.jar

EOF

jar --create \
    --file "$build_dir/input/ByteBound.jar" \
    --manifest "$build_dir/manifest.mf" \
    -C "$build_dir/classes" .
cp lib/jlayer-1.0.1.jar "$build_dir/input/"

jpackage \
    --type app-image \
    --name ByteBound \
    --app-version 1.0.0 \
    --input "$build_dir/input" \
    --main-jar ByteBound.jar \
    --main-class ByteBound \
    --dest "$build_dir/package"

mkdir -p dist
tar -czf "$build_dir/ByteBound-linux-x86_64.tar.gz" \
    -C "$build_dir/package" ByteBound
cp "$build_dir/ByteBound-linux-x86_64.tar.gz" dist/
printf 'Created %s\n' "dist/ByteBound-linux-x86_64.tar.gz"

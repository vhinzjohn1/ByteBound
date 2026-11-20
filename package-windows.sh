#!/bin/sh
set -eu

if [ "$(uname -s)" != "Linux" ] || [ "$(uname -m)" != "x86_64" ]; then
    echo "This packaging script builds Windows x64 only from Linux x86_64." >&2
    exit 1
fi

cd "$(dirname "$0")"
build_dir=$(mktemp -d "${TMPDIR:-/tmp}/bytebound-windows.XXXXXX")
trap 'rm -rf "$build_dir"' EXIT HUP INT TERM

jre_url="https://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.13%2B9/OpenJDK21U-jre_x64_windows_hotspot_21.0.13_9.zip"
jre_sha256="2332c0b4cff6bed722da3567c91d5e47568bcba863fa4945d0a8d6fc76158628"

mkdir -p "$build_dir/classes" "$build_dir/package/ByteBound/runtime"
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
    --file "$build_dir/package/ByteBound/ByteBound.jar" \
    --manifest "$build_dir/manifest.mf" \
    -C "$build_dir/classes" .
cp lib/jlayer-1.0.1.jar "$build_dir/package/ByteBound/"

curl --fail --location --retry 3 "$jre_url" -o "$build_dir/temurin-jre.zip"
printf '%s  %s\n' "$jre_sha256" "$build_dir/temurin-jre.zip" | sha256sum --check
unzip -q "$build_dir/temurin-jre.zip" -d "$build_dir/package/ByteBound/runtime"

printf '@echo off\r\nsetlocal\r\ncd /d "%%~dp0"\r\nstart "" "%%~dp0runtime\\jdk-21.0.13+9-jre\\bin\\javaw.exe" -jar "%%~dp0ByteBound.jar"\r\n' \
    > "$build_dir/package/ByteBound/run.bat"

mkdir -p dist
(
    cd "$build_dir/package"
    zip -qr "$build_dir/ByteBound-windows-x64.zip" ByteBound
)
cp "$build_dir/ByteBound-windows-x64.zip" dist/
printf 'Created %s\n' "dist/ByteBound-windows-x64.zip"

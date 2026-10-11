#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT to your Android SDK directory}"
bt="$ANDROID_SDK_ROOT/build-tools/35.0.0"
platform="$ANDROID_SDK_ROOT/platforms/android-35/android.jar"
rm -rf build/classes build/dex
mkdir -p build/classes build/dex
"$bt/aapt2" compile --dir res -o build/resources.zip
"$bt/aapt2" link -o build/base.apk -I "$platform" --manifest AndroidManifest.xml build/resources.zip
java com.sun.tools.javac.Main -source 8 -target 8 -classpath "$platform" -d build/classes src/com/eightbittrade/notepad98/*.java
java sun.tools.jar.Main cf build/classes.jar -C build/classes .
"$bt/d8" --lib "$platform" --min-api 26 --output build/dex build/classes.jar
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$bt/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
if [[ "${1:-}" == "--unsigned" ]]; then
  echo "Unsigned build: build/aligned.apk (not a public release)"
  exit 0
fi
: "${NOTEPAD_KEYSTORE:?Set NOTEPAD_KEYSTORE to your private signing keystore path}"
: "${NOTEPAD_STORE_PASS:?Set NOTEPAD_STORE_PASS}"
: "${NOTEPAD_KEY_PASS:?Set NOTEPAD_KEY_PASS}"
export NOTEPAD_STORE_PASS NOTEPAD_KEY_PASS
signed_output="$(mktemp build/signed-XXXXXX.apk)"
"$bt/apksigner" sign --ks "$NOTEPAD_KEYSTORE" --ks-pass env:NOTEPAD_STORE_PASS --key-pass env:NOTEPAD_KEY_PASS --out "$signed_output" build/aligned.apk
mv "$signed_output" Notepad98.apk
"$bt/apksigner" verify --verbose Notepad98.apk

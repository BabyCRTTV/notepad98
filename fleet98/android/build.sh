#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${ANDROID_SDK_ROOT:?Set ANDROID_SDK_ROOT to your Android SDK directory}"
bt="$ANDROID_SDK_ROOT/build-tools/35.0.0"
platform="$ANDROID_SDK_ROOT/platforms/android-35/android.jar"
mkdir -p build/classes build/dex build/assets/web
cp ../web/* build/assets/web/
"$bt/aapt2" compile --dir app/src/main/res -o build/resources.zip
"$bt/aapt2" link -o build/base.apk -I "$platform" --manifest app/src/main/AndroidManifest.xml -A build/assets build/resources.zip
java com.sun.tools.javac.Main -source 8 -target 8 -classpath "$platform" -d build/classes app/src/main/java/shop/eightbittrade/fleet98/MainActivity.java
java sun.tools.jar.Main cf build/classes.jar -C build/classes .
"$bt/d8" --lib "$platform" --min-api 26 --output build/dex build/classes.jar
cp build/base.apk build/unsigned.apk
(cd build/dex && zip -q ../unsigned.apk classes.dex)
"$bt/zipalign" -f -p 4 build/unsigned.apk build/aligned.apk
if [ -n "${FLEET98_KEYSTORE:-}" ]; then
 : "${FLEET98_STORE_PASS:?Set keystore password}"
 : "${FLEET98_KEY_PASS:?Set key password}"
 "$bt/apksigner" sign --ks "$FLEET98_KEYSTORE" --ks-pass env:FLEET98_STORE_PASS --key-pass env:FLEET98_KEY_PASS --out build/Fleet98.apk build/aligned.apk
else
 if [ ! -f build/debug.keystore ]; then keytool -genkeypair -keystore build/debug.keystore -storepass android -keypass android -alias androiddebugkey -dname 'CN=Android Debug,O=Android,C=US' -keyalg RSA -keysize 2048 -validity 10000; fi
 "$bt/apksigner" sign --ks build/debug.keystore --ks-pass pass:android --key-pass pass:android --out build/Fleet98-debug.apk build/aligned.apk
fi
"$bt/apksigner" verify build/Fleet98*.apk

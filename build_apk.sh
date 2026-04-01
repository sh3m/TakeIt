#!/bin/bash
set -e

# ── Paths ────────────────────────────────────────────────────────────────────
SDK=/usr/lib/android-sdk
PLATFORM=$SDK/platforms/android-23
ANDROID_JAR=$PLATFORM/android.jar
AAPT2=$SDK/build-tools/debian/aapt2
DX=$SDK/build-tools/debian/dx
APKSIGNER=$SDK/build-tools/debian/apksigner
ZIPALIGN=$SDK/build-tools/debian/zipalign
BUILD_DIR=/tmp/reminder_build
SRC=app/src/main/java
MANIFEST=app/src/main/AndroidManifest.xml
RES=app/src/main/res
PKG=com/example/takeit

echo "=== Cleaning build dir ==="
rm -rf $BUILD_DIR
mkdir -p $BUILD_DIR/{res_flat,gen,classes}

echo "=== Compiling resources with aapt2 ==="
$AAPT2 compile --dir $RES -o $BUILD_DIR/res_flat/res.zip

echo "=== Linking resources ==="
$AAPT2 link \
  -o $BUILD_DIR/app-unsigned.apk \
  -I $ANDROID_JAR \
  --manifest $MANIFEST \
  --java $BUILD_DIR/gen/ \
  --min-sdk-version 21 \
  --target-sdk-version 34 \
  --version-code 1 \
  --version-name "1.0" \
  $BUILD_DIR/res_flat/res.zip

echo "=== Compiling Java ==="
find $SRC -name "*.java" > /tmp/sources.txt
cat $BUILD_DIR/gen/$PKG/R.java >> /dev/null && echo "R.java found"
javac \
  -source 1.8 -target 1.8 \
  -bootclasspath $ANDROID_JAR \
  -classpath $ANDROID_JAR \
  -d $BUILD_DIR/classes/ \
  @/tmp/sources.txt $BUILD_DIR/gen/$PKG/R.java

echo "=== Converting to DEX ==="
$DX --dex --output=$BUILD_DIR/classes.dex $BUILD_DIR/classes/

echo "=== Adding DEX to APK ==="
cd $BUILD_DIR
zip -j app-unsigned.apk classes.dex
cd -

echo "=== Zipalign ==="
$ZIPALIGN -f 4 $BUILD_DIR/app-unsigned.apk $BUILD_DIR/app-aligned.apk

echo "=== Creating debug keystore ==="
if [ ! -f /tmp/debug.keystore ]; then
  keytool -genkeypair -v \
    -keystore /tmp/debug.keystore \
    -alias androiddebugkey \
    -keyalg RSA -keysize 2048 \
    -validity 10000 \
    -storepass android -keypass android \
    -dname "CN=Android Debug,O=Android,C=US" \
    2>&1 | grep -v "^Picked up\|Warning:"
fi

echo "=== Signing APK ==="
$APKSIGNER sign \
  --ks /tmp/debug.keystore \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out app-debug.apk \
  $BUILD_DIR/app-aligned.apk

echo ""
echo "Build complete: $(pwd)/app-debug.apk"
ls -lh app-debug.apk

#!/data/data/com.termux/files/usr/bin/bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
JAVA_HOME="${JAVA_HOME:-/data/data/com.termux/files/usr/lib/jvm/java-17-openjdk}"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

ANDROID_JAR="${ANDROID_JAR:-$ROOT/tools/android.jar}"
if [ ! -f "$ANDROID_JAR" ]; then
  mkdir -p "$ROOT/tools"
  echo "downloading android.jar..."
  curl -L --fail -o "$ANDROID_JAR" \
    "https://github.com/Sable/android-platforms/raw/master/android-33/android.jar"
fi

rm -rf "$ROOT/build"
mkdir -p "$ROOT/build/gen" "$ROOT/build/classes" "$ROOT/build/dex"

echo "aapt package..."
aapt package -f \
  -M "$ROOT/app/src/main/AndroidManifest.xml" \
  -S "$ROOT/app/src/main/res" \
  -A "$ROOT/app/src/main/assets" \
  -I "$ANDROID_JAR" \
  -F "$ROOT/build/unaligned.apk" \
  -J "$ROOT/build/gen" \
  --min-sdk-version 26 \
  --target-sdk-version 33 \
  --version-code 1 \
  --version-name 1.0.0

echo "javac..."
find "$ROOT/app/src/main/java" "$ROOT/build/gen" -name '*.java' > "$ROOT/build/sources.txt"
javac --release 11 \
  -encoding UTF-8 \
  -cp "$ANDROID_JAR" \
  -d "$ROOT/build/classes" \
  @"$ROOT/build/sources.txt"

echo "d8..."
find "$ROOT/build/classes" -name '*.class' > "$ROOT/build/classes.txt"
# d8 wants class files or a jar
jar cf "$ROOT/build/classes.jar" -C "$ROOT/build/classes" .
d8 --min-api 26 --lib "$ANDROID_JAR" --output "$ROOT/build/dex" "$ROOT/build/classes.jar"

echo "add dex..."
cp "$ROOT/build/unaligned.apk" "$ROOT/build/withdex.apk"
( cd "$ROOT/build/dex" && aapt add "$ROOT/build/withdex.apk" classes.dex )

KS="$ROOT/tools/debug.keystore"
if [ ! -f "$KS" ]; then
  mkdir -p "$ROOT/tools"
  keytool -genkeypair -v -keystore "$KS" -storepass android -alias androiddebugkey \
    -keypass android -keyalg RSA -keysize 2048 -validity 10000 \
    -dname "CN=UnlockCards,O=MrHotIce,C=RU"
fi

OUT="$ROOT/UnlockCards-1.0.0.apk"
echo "sign..."
apksigner sign --ks "$KS" --ks-pass pass:android --ks-key-alias androiddebugkey \
  --key-pass pass:android --out "$OUT" "$ROOT/build/withdex.apk"
apksigner verify --verbose "$OUT" || true
ls -lh "$OUT"
echo "OK $OUT"

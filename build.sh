#!/usr/bin/env bash
set -e

echo ""
echo "╔══════════════════════════════════════════════════════════╗"
echo "║         BarPrep NG — Android Build Script                ║"
echo "╚══════════════════════════════════════════════════════════╝"
echo ""

# ── Check requirements ─────────────────────────────────────────────────────
check_cmd() {
  if ! command -v "$1" &>/dev/null; then
    echo "  ✗  $1 not found — $2"
    exit 1
  fi
  echo "  ✓  $1 found"
}

echo "▸ Checking dependencies..."
check_cmd java "Install JDK 17: https://adoptium.net"
check_cmd git "Install git: https://git-scm.com"

JAVA_VER=$(java -version 2>&1 | head -1 | grep -oP '(?<=version ")[0-9]+')
if [ "$JAVA_VER" -lt 17 ] 2>/dev/null; then
  echo "  ✗  Java 17+ required (found Java $JAVA_VER)"
  echo "     Download: https://adoptium.net/temurin/releases/?version=17"
  exit 1
fi
echo "  ✓  Java $JAVA_VER"

# ── Download fonts ──────────────────────────────────────────────────────────
echo ""
echo "▸ Downloading Inter fonts..."
FONT_DIR="app/src/main/res/font"

download_font() {
  local URL="$1" FILE="$2"
  if [ ! -s "$FONT_DIR/$FILE" ]; then
    if command -v curl &>/dev/null; then
      curl -fsSL "$URL" -o "$FONT_DIR/$FILE" && echo "  ✓  $FILE" || echo "  ⚠  Could not download $FILE (continuing without it)"
    elif command -v wget &>/dev/null; then
      wget -q "$URL" -O "$FONT_DIR/$FILE" && echo "  ✓  $FILE" || echo "  ⚠  Could not download $FILE (continuing without it)"
    else
      echo "  ⚠  curl/wget not found — skipping font download"
    fi
  else
    echo "  ✓  $FILE (cached)"
  fi
}

# Inter font files from Google Fonts GitHub (open source, OFL license)
BASE="https://github.com/rsms/inter/raw/master/docs/font-files"
download_font "$BASE/Inter-Regular.ttf"  "inter_regular.ttf"
download_font "$BASE/Inter-SemiBold.ttf" "inter_semibold.ttf"
download_font "$BASE/Inter-Bold.ttf"     "inter_bold.ttf"

# ── ANDROID_HOME check ─────────────────────────────────────────────────────
echo ""
echo "▸ Checking Android SDK..."
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
  # Try common locations
  for candidate in \
    "$HOME/Android/Sdk" \
    "$HOME/Library/Android/sdk" \
    "/opt/android-sdk" \
    "/usr/local/lib/android/sdk"; do
    if [ -d "$candidate" ]; then
      export ANDROID_HOME="$candidate"
      echo "  ✓  Found SDK at $ANDROID_HOME"
      break
    fi
  done
fi

if [ -z "$ANDROID_HOME" ]; then
  echo "  ✗  ANDROID_HOME not set and SDK not found in common locations."
  echo ""
  echo "     Option A — Set it manually:"
  echo "       export ANDROID_HOME=/path/to/your/android/sdk"
  echo "       ./build.sh"
  echo ""
  echo "     Option B — Install Android Studio (includes SDK):"
  echo "       https://developer.android.com/studio"
  echo ""
  echo "     Option C — Install command-line tools only:"
  echo "       https://developer.android.com/studio#command-tools"
  echo ""
  exit 1
fi
echo "  ✓  ANDROID_HOME = $ANDROID_HOME"

# ── Make gradlew executable ────────────────────────────────────────────────
echo ""
echo "▸ Setting up Gradle wrapper..."
chmod +x gradlew 2>/dev/null || true

# If gradlew doesn't exist, bootstrap it
if [ ! -f "gradlew" ]; then
  echo "  ▸ Generating gradlew..."
  # Download gradle wrapper jar
  mkdir -p gradle/wrapper
  WRAPPER_URL="https://services.gradle.org/distributions/gradle-8.2-bin.zip"
  if command -v curl &>/dev/null; then
    curl -fsSL "https://raw.githubusercontent.com/gradle/gradle/master/gradlew" -o gradlew
    chmod +x gradlew
  fi
fi

# ── Build ──────────────────────────────────────────────────────────────────
echo ""
echo "▸ Building debug APK (this takes ~2-3 minutes on first run)..."
echo ""

./gradlew assembleDebug \
  --no-daemon \
  --console=plain \
  --warning-mode=none \
  -Dorg.gradle.jvmargs="-Xmx1536m" \
  2>&1 | grep -E "(BUILD|FAILURE|ERROR|error:|warning:|Downloading|Compiling|Merging|Packaging|Task :)" || true

# ── Output ─────────────────────────────────────────────────────────────────
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
if [ -f "$APK_PATH" ]; then
  SIZE=$(du -h "$APK_PATH" | cut -f1)
  echo ""
  echo "╔══════════════════════════════════════════════════════════╗"
  echo "║  ✅  BUILD SUCCESSFUL                                    ║"
  echo "╚══════════════════════════════════════════════════════════╝"
  echo ""
  echo "  APK:  $APK_PATH"
  echo "  Size: $SIZE"
  echo ""
  echo "▸ Install on connected device / emulator:"
  echo "  adb install -r $APK_PATH"
  echo ""
  echo "▸ Or copy the APK to your phone and install manually."
  echo ""
else
  echo ""
  echo "╔══════════════════════════════════════════════════════════╗"
  echo "║  ✗  BUILD FAILED                                         ║"
  echo "╚══════════════════════════════════════════════════════════╝"
  echo ""
  echo "  Run with full output:"
  echo "  ./gradlew assembleDebug --stacktrace"
  echo ""
  exit 1
fi

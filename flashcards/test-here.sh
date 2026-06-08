#!/usr/bin/env bash
set -euo pipefail

# Run the Flashcards Android app in this cloud workspace using a headless emulator.
# Requires ~6GB RAM and 10+ minutes on first run (SDK download + emulator boot).

ANDROID_HOME="${ANDROID_HOME:-$HOME/android-sdk}"
export ANDROID_HOME
export PATH="$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SDK_DIR="$ANDROID_HOME"
LOCAL_PROPS="$PROJECT_DIR/local.properties"

echo "==> Android SDK: $SDK_DIR"

if [ ! -d "$SDK_DIR/cmdline-tools/latest" ]; then
  echo "==> Installing Android command-line tools..."
  mkdir -p "$SDK_DIR/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/cmdline-tools.zip" \
    https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
  unzip -q "$tmp/cmdline-tools.zip" -d "$tmp"
  rm -rf "$SDK_DIR/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$SDK_DIR/cmdline-tools/latest"
  yes | sdkmanager --sdk_root="$SDK_DIR" --licenses >/dev/null
  sdkmanager --sdk_root="$SDK_DIR" \
    "platform-tools" "platforms;android-35" "build-tools;35.0.0" \
    "emulator" "system-images;android-34;google_apis;x86_64"
fi

echo "sdk.dir=$SDK_DIR" > "$LOCAL_PROPS"

echo "==> Building debug APK..."
cd "$PROJECT_DIR"
./gradlew assembleDebug --no-daemon -q

if ! adb devices 2>/dev/null | grep -q "emulator.*device"; then
  echo "==> Starting headless emulator (no KVM — expect slow boot)..."
  if ! avdmanager list avd 2>/dev/null | grep -q "flashcards_test"; then
    echo no | avdmanager create avd -n flashcards_test \
      -k "system-images;android-34;google_apis;x86_64" --device pixel_7 --force
  fi
  SESSION="android-emulator"
  if ! tmux -f /exec-daemon/tmux.portal.conf has-session -t "=$SESSION" 2>/dev/null; then
    tmux -f /exec-daemon/tmux.portal.conf new-session -d -s "$SESSION" -- \
      emulator -avd flashcards_test -no-window -no-audio -no-boot-anim \
        -gpu swiftshader_indirect -no-snapshot -no-accel
  fi
  echo "==> Waiting for emulator boot (can take 5–8 min without hardware acceleration)..."
  adb wait-for-device shell 'while [ -z "$(getprop sys.boot_completed)" ]; do sleep 3; done'
fi

APK="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
echo "==> Installing and launching app..."
adb install -r "$APK"
adb shell am start -n com.flashcards.app/.MainActivity

echo "==> Waiting for Compose UI (slow on software emulator)..."
sleep 45

mkdir -p "$PROJECT_DIR/screenshots"
adb exec-out screencap -p > "$PROJECT_DIR/screenshots/latest.png"
echo "==> Screenshot saved to screenshots/latest.png"
echo "==> App is running on emulator-5554"
echo ""
echo "Useful commands:"
echo "  adb exec-out screencap -p > screenshots/screen.png   # screenshot"
echo "  adb shell input tap X Y                               # tap coordinates"
echo "  adb shell am force-stop com.flashcards.app            # stop app"
echo "  adb shell am start -n com.flashcards.app/.MainActivity # relaunch"

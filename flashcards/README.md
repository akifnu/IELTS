# Flashcards — Android App

A native Android flashcard app built with **Kotlin**, **Jetpack Compose**, and **Room**.

## Features

Identical to the [live web app](https://cdn.jsdelivr.net/gh/akifnu/IELTS@gh-pages/index.html): clusters, decks, cards, study modes, Ebbinghaus / Leitner / SM-2, calendar, sharing, account, splash, and backup. Built from the same `flashcards/web/` source on every APK release.

## Screenshots (flow)

1. **Deck List** — all your decks with card counts
2. **Deck Detail** — manage cards and start a study session
3. **Study Session** — flip cards, rate yourself, see results

## Requirements

- Android Studio Ladybug (2024.2.1) or newer
- JDK 17
- Android SDK 35
- Min SDK 26 (Android 8.0)

## Build & Run

1. Open the `flashcards/` folder in Android Studio
2. Let Gradle sync complete
3. Run on an emulator or physical device (▶ Run)

Or from the command line (with Android SDK installed):

```bash
cd flashcards
./gradlew assembleDebug
```

The release APK will be at `app/build/outputs/apk/release/app-release.apk`.

### Install on your phone

Download the latest **Shine** APK from [GitHub Releases](https://github.com/akifnu/IELTS/releases/latest) (`Shine-Flashcards.apk`, currently **v2.2.0**).

1. Transfer the file to your phone (download link, email, USB, etc.)
2. Open the APK and allow **Install unknown apps** when prompted
3. Tap **Install**

The Android APK bundles the **same Shine web app** as the website (`flashcards/web/`) for **1:1 feature parity** — clusters, spaced repetition, calendar, sharing, Google Sign-In, splash, and all UI. A native shell handles screen insets and file sharing.

Build locally:

```bash
cd flashcards
./gradlew assembleRelease
cp app/build/outputs/apk/release/app-release.apk Shine-Flashcards.apk
```

## Test in this cloud workspace

This environment has no display and no hardware virtualization (KVM), so the emulator runs in **software mode** and is slow (~5–8 min to boot). It works, but expect occasional "System isn't responding" dialogs — tap **Wait**.

**One-command test** (installs SDK on first run, builds, boots emulator, launches app):

```bash
cd flashcards
./test-here.sh
```

**If the emulator is already running** (faster relaunch):

```bash
export ANDROID_HOME="$HOME/android-sdk"
export PATH="$ANDROID_HOME/platform-tools:$ANDROID_HOME/emulator:$PATH"

cd flashcards
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.flashcards.app/.MainActivity

# Wait ~45s for Compose to render on the slow emulator, then screenshot:
sleep 45
adb exec-out screencap -p > screenshots/latest.png
```

**Interact via adb** (1080×2400 screen):

```bash
adb shell input tap 540 1050   # open Spanish Basics deck
adb shell input tap 540 350    # tap Study button
adb exec-out screencap -p > screenshots/screen.png
```

## Tech Stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM + Repository |
| Database | Room (SQLite) |
| Navigation | Navigation Compose |
| Language | Kotlin |

## Project Structure

```
app/src/main/java/com/flashcards/app/
├── data/           # Room entities, DAOs, repository
├── navigation/     # Route definitions
├── ui/
│   ├── components/ # Reusable UI (flip card, dialogs)
│   ├── screens/    # Deck list, detail, study
│   └── theme/      # Material 3 theme
├── viewmodel/      # ViewModels for each screen
├── FlashcardsApp.kt
└── MainActivity.kt
```

## License

MIT

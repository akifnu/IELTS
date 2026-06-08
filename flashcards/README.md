# Flashcards — Android App

A native Android flashcard app built with **Kotlin**, **Jetpack Compose**, and **Room**.

## Features

- **Deck management** — create, view, and delete flashcard decks
- **Card editor** — add, edit, and delete cards (front/back)
- **Study mode** — flip cards with animation, mark correct/incorrect, track progress
- **Persistent storage** — all data saved locally with Room SQLite
- **Sample data** — two starter decks (Spanish Basics & Kotlin) on first launch

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

The debug APK will be at `app/build/outputs/apk/debug/app-debug.apk`.

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

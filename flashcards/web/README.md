# Shine — Lifestyle Flashcards (Web)

Shine is a installable web app for organizing knowledge in **clusters**, building **decks**, and studying with **spaced repetition** (Ebbinghaus, Leitner, SM-2).

## Run locally

```bash
cd flashcards/web
./serve.sh
```

Open **http://localhost:8080**

## Deploy for production (recommended)

Host the `flashcards/web/` folder as a **static site** with HTTPS.

### Netlify / Vercel / GitHub Pages

- Publish directory: `flashcards/web`
- No build step required
- `netlify.toml` is included for Netlify

### Google Sign-In (Gmail one-click)

1. [Google Cloud Console](https://console.cloud.google.com/apis/credentials) → **OAuth 2.0 Client ID** → **Web application**
2. Add your production URL under **Authorized JavaScript origins** (e.g. `https://your-app.netlify.app`)
3. Copy `auth-config.example.js` → `auth-config.js` and set:

```js
window.SHINE_GOOGLE_CLIENT_ID = 'YOUR_CLIENT_ID.apps.googleusercontent.com';
```

4. Redeploy. Users tap **Continue with Google** on splash or Account.

> Google Sign-In requires HTTPS in production (localhost is allowed for dev).

## Features

| Area | Details |
|------|---------|
| Clusters | Group decks by topic (Languages, Wellness, etc.) |
| Cards | Colors, drag reorder, shuffle |
| Study | Flip cards, spaced repetition algorithms |
| Calendar | Global schedule, busy-day warnings |
| Account | Gmail or email sign-in, export/import JSON backup |
| PWA | Installable, offline shell via service worker |

## Data & privacy

- Accounts and decks are stored in the browser (`localStorage`) on the user’s device
- Email passwords are hashed with PBKDF2 before storage
- Use **Export** on the Account tab for backups
- For multi-device sync, a backend (Firebase, Supabase, etc.) can be added later

## Files

| File | Purpose |
|------|---------|
| `index.html` | Full app |
| `manifest.json` | PWA manifest |
| `sw.js` | Offline cache |
| `auth-config.js` | Google OAuth Client ID (not committed with secrets) |
| `icons/icon.svg` | App icon |

## Android

See `../README.md` for the native Kotlin app (separate from this web build).

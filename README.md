# 🎬 Movie Magic

An offline-first Android movie app built with **Jetpack Compose**. Browse popular movies from [TMDB](https://www.themoviedb.org/), search the full catalog, keep a personal movie log with ratings and notes, and favorite titles that sync across devices via Firebase.

<!-- Add your screenshots to docs/screenshots/ and update the file names below -->
<p align="center">
  <img src="docs/screenshots/home.png" width="24%" alt="Home screen" />
  <img src="docs/screenshots/detail.png" width="24%" alt="Movie detail" />
  <img src="docs/screenshots/search.png" width="24%" alt="Search" />
  <img src="docs/screenshots/movie-log.png" width="24%" alt="Movie log" />
</p>

## Features

- **Browse & search** — popular movies and full-text search powered by the TMDB API
- **Movie log** — track what you've watched with your own ratings and notes
- **Favorites** — save movies locally and sync them to the cloud
- **Auth** — email/password and Google Sign-In via Firebase Authentication
- **Offline-first** — Room caches everything locally; a custom sync engine reconciles with Firebase when you're back online
- **Resilient networking** — automatic retry interceptor and live network monitoring

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material 3, Navigation Compose, Coil |
| Architecture | MVVM + Repository pattern |
| Dependency Injection | Hilt |
| Local storage | Room (with migrations) |
| Remote | Retrofit + Gson (TMDB API), Firebase Realtime Database & Storage |
| Auth | Firebase Auth + Google Sign-In |
| Async | Kotlin Coroutines & Flow |
| Native | NDK (C++) for API key protection |
| Testing | JUnit, Mockito, Compose UI tests, Room in-memory tests |

## Architecture

The app follows **MVVM + Repository** with a clean separation between layers:

```
views/          Compose screens + ViewModels (home, search, detail, favorites, movie log, auth, profile)
data/
  repository/   Single source of truth per feature (Movie, Favorites, MovieLog, Auth, Users)
  local/        Room database, DAOs, entities, migrations
  sync/         SyncManager + FirebaseSyncEngine for offline-first cloud sync
networking/     Retrofit service + retry interceptor
models/         DTOs (network) and domain models, with explicit mappers
di/             Hilt modules (Network, Database, Firebase, Sync, Dispatchers)
```

A full system-design writeup with an editable Mermaid diagram lives in [`docs/system-design.md`](docs/system-design.md).

### Offline-first sync

Room is the single source of truth — the UI only ever reads from the local database. Writes land locally first, then a `SyncManager` pushes pending changes to Firebase through a generic `Syncable`/`SyncableStore` abstraction whenever connectivity is available, so the app is fully usable offline.

## Getting Started

### Prerequisites

- Android Studio (latest stable)
- JDK 11+
- Android NDK (installed via SDK Manager — the project uses `ndk-build`)
- A [TMDB API key](https://developer.themoviedb.org/docs/getting-started)
- A [Firebase project](https://console.firebase.google.com/) with Authentication, Realtime Database, and Storage enabled

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/Darnell-O/Movie_Project.git
   ```
2. Add your Firebase config: download `google-services.json` from the Firebase console and place it in `app/`.
3. Add your TMDB API key in `app/src/main/jni/native-lib.cpp` (the key is compiled into a native library rather than stored in plain Kotlin).
4. Open the project in Android Studio, let Gradle sync, and run on a device or emulator (min SDK 24).

## Testing

```bash
./gradlew test                  # Unit tests (JUnit + Mockito + coroutines-test)
./gradlew connectedAndroidTest  # Instrumented tests (Compose UI + Room in-memory)
```

## License

This project was built for learning purposes. Movie data provided by [TMDB](https://www.themoviedb.org/) — this product uses the TMDB API but is not endorsed or certified by TMDB.

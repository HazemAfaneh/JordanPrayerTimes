# Jordan Prayer Times

A Wear OS app that displays Islamic prayer times for cities across Jordan. Shows the next upcoming prayer, sends notifications at prayer times, and updates a watch face complication automatically.

---

## Features

- Prayer times for the current day (Fajr, Dhuhr, Asr, Maghrib, Isha)
- Countdown timer to the next prayer
- City selection from all supported Jordanian cities
- Prayer notifications delivered at the exact alarm time
- Watch face **complication** (SHORT_TEXT) showing next prayer name and time — updates automatically when a prayer alarm fires
- Wear OS **tile** for at-a-glance prayer schedule
- Notification toggle in Settings
- Monthly background data refresh via WorkManager
- Offline caching — data is available without a network connection after first load

---

## Screenshots

> _Add watch face screenshots here_

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose for Wear OS, Wear Material, Horologist |
| Navigation | Wear Compose Navigation |
| Networking | Ktor 2.3.7 (OkHttp engine) + Kotlinx Serialization |
| DI | Koin 4.0.0 |
| Background work | WorkManager 2.9.1 |
| Alarms | AlarmManager (exact, allow-while-idle) |
| Complication | `SuspendingComplicationDataSourceService` (watchface-complications-data-source-ktx) |
| Tile | Wear Tiles + Horologist Tiles |
| Async | Kotlin Coroutines + Flow |
| Min SDK | 30 (Wear OS 3) |
| Target SDK | 34 |
| Kotlin | 2.0.0 |

---

## Architecture

Clean Architecture with three layers and MVVM in the presentation layer.

```
Presentation  →  Domain (Use Cases)  →  Data (Repositories + Sources)
```

```
app/src/main/java/com/mbf/jordan_prayer_times_app/
├── complication/        Watch face complication service
├── tile/                Wear OS tile service
├── notification/        Alarm receiver, scheduler, notification helper
├── worker/              WorkManager refresh worker + scheduler
├── presentation/
│   ├── screens/         MainScreen, SettingsScreen, CommonComponents
│   ├── theme/
│   ├── MainActivity.kt
│   ├── MainViewModel.kt
│   └── SettingsViewModel.kt
├── usecase/             LoadInitialHomeScreenDataUseCase, LoadPrayerTimesForCityUseCase
├── repositories/        LoadPrayerTimesForCityRepo, LoadCitiesRepo
├── data/
│   ├── remote/          Ktor data sources (prayer times + cities)
│   └── local/           MonthlyPrayerCache (SharedPreferences), CityPreferences
└── di/                  Koin modules
```

See [ARCHITECTURE.md](ARCHITECTURE.md) and [KOIN_DEPENDENCY_INJECTION.md](KOIN_DEPENDENCY_INJECTION.md) for detailed documentation.

---

## Data Source

Prayer times are fetched from a GitHub-hosted JSON dataset:

- **Repo:** `mbanifawaz/Jordan_Prayer_Times_API_Data`
- **Cities endpoint:** `contents/cities/cities.json`
- **Prayer times endpoint:** `contents/monthly/{year}_{month}_{city}.json`
- **Auth:** GitHub personal access token (required — see Setup)

Data is cached locally after each fetch. WorkManager refreshes it automatically at the start of each month.

---

## Setup

### 1. GitHub Token

The app requires a GitHub personal access token to fetch prayer data from the private dataset repo.

Create `local.properties` in the project root (if it doesn't already exist) and add:

```properties
github_token=your_github_token_here
```

### 2. Signing (Release Builds)

Create `key.properties` in the project root:

```properties
storeFile=/absolute/path/to/your.keystore
storePassword=your_store_password
keyAlias=your_key_alias
keyPassword=your_key_password
```

Debug builds do not require this file.

### 3. Build & Install

```bash
./gradlew installDebug
```

Or open the project in Android Studio and run on a paired Wear OS device or emulator (API 30+).

---

## How Complication Updates Work

The complication is updated in three places:

| Trigger | Location |
|---|---|
| User selects a city or loads initial data | `MainViewModel.refreshComplication()` |
| Prayer alarm fires (notification sent) | `PrayerAlarmReceiver.onReceive()` |
| Watch face requests data | `MainComplicationService.onComplicationRequest()` |

This ensures the complication always reflects the current next prayer without requiring the user to open the app.

---

## Permissions

| Permission | Purpose |
|---|---|
| `POST_NOTIFICATIONS` | Prayer time notifications (Android 13+) |
| `SCHEDULE_EXACT_ALARM` | Exact prayer alarms (Android 12+) |
| `RECEIVE_BOOT_COMPLETED` | Reschedule alarms after device reboot |
| `INTERNET` | Fetch prayer times from GitHub |
| `WAKE_LOCK` | Keep device awake during alarm delivery |

---

## Version

**2.3.0** (versionCode 14)

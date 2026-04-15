# BioBeat

Android app for real-time health monitoring via Bluetooth Low Energy wearable devices. Streams live heart rate and ECG data, displays device notifications and status, and provides device settings control.

## Project Structure

The project is split into two Gradle modules:

```
biobeat/
├── app/                  (:app)           — Android application
├── biobeat-sdk/          (:biobeat-sdk)    — BLE communication library
├── gradle/libs.versions.toml
└── settings.gradle.kts
```

**`:biobeat-sdk`** — Standalone BLE SDK that handles all Bluetooth communication with BioBeat wearable devices. It has no UI, no database, and no dependency injection — any Android app can consume it. See [biobeat-sdk/README.md](biobeat-sdk/README.md) for full SDK documentation.

**`:app`** — The BioBeat application, built on top of the SDK with Clean Architecture (UI / Domain / Data layers), Jetpack Compose, Hilt, and Room.

## Architecture

```
┌─────────────────────────────────────────────────────┐
│  :app                                               │
│  ┌──────────┐  ┌──────────┐  ┌────────────────────┐ │
│  │    UI    │→│  Domain  │→│       Data         │ │
│  │ Compose  │  │ UseCases │  │ Room + Repositories│ │
│  └──────────┘  └──────────┘  └────────┬───────────┘ │
│                                       │             │
├───────────────────────────────────────┼─────────────┤
│  :biobeat-sdk                         │             │
│  ┌────────────────────────────────────▼───────────┐ │
│  │         BioBeatSdk / DeviceConnection          │ │
│  │      BLE scanning, connect, live streaming     │ │
│  └────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────┘
```

### App layers

| Layer | Responsibility | Key classes |
|-------|---------------|-------------|
| **UI** | Jetpack Compose screens, ViewModels | `ScanScreen`, `DashboardScreen`, `EcgScreen`, `HistoryScreen`, `NotificationsScreen`, `DeviceSettingsScreen` |
| **Domain** | Business logic, platform-independent | `ConnectDeviceUseCase`, `ObserveHeartRateUseCase`, `RecordEcgSessionUseCase`, `CheckHealthAlertUseCase` |
| **Data** | SDK wrapper, Room persistence, mappers | `DeviceRepositoryImpl`, `ReadingRepositoryImpl`, `BioBeatDatabase` |

## Screens

| Screen | Description |
|--------|-------------|
| **Scan** | Discovers nearby BLE devices, handles runtime permission requests |
| **Dashboard** | Live heart rate display, battery/memory status, health alerts, quick navigation |
| **ECG** | Real-time ECG waveform rendering on Canvas (~250 Hz) |
| **History** | Browse stored heart rate readings and ECG sessions (Room DB) |
| **Notifications** | Device-pushed alerts with severity-based styling |
| **Settings** | Read/write device configuration (ECG toggle, HR interval, device name) |

## Tech Stack

| Component | Version |
|-----------|---------|
| AGP | 9.1.1 |
| Kotlin | 2.1.20 (bundled with AGP) |
| Min SDK | 26 (Android 8.0) |
| Target / Compile SDK | 36 |
| Jetpack Compose BOM | 2025.04.00 |
| Hilt | 2.59.2 |
| Room | 2.7.1 |
| Coroutines | 1.10.2 |
| Gradle | 9.3.1 |

## Requirements

- Android Studio Narwhal (2025.1) or later (for AGP 9.x support)
- JDK 17+
- A physical Android device for BLE testing (emulator does not support real BLE)

## Build

```bash
# Debug build
./gradlew assembleDebug

# Run all tests
./gradlew test

# SDK tests only
./gradlew :biobeat-sdk:test

# App tests only
./gradlew :app:test

# Check SDK has no unintended dependencies
./gradlew :biobeat-sdk:dependencies --configuration releaseRuntimeClasspath
```

## Permissions

The app requires the following permissions at runtime:

| Permission | When | API level |
|-----------|------|-----------|
| `BLUETOOTH_SCAN` | Before scanning for devices | 31+ |
| `BLUETOOTH_CONNECT` | Before connecting to a device | 31+ |
| `ACCESS_FINE_LOCATION` | Before scanning (location required for BLE on older Android) | 26–30 |
| `POST_NOTIFICATIONS` | For foreground service notification | 33+ |

The scan screen handles `BLUETOOTH_SCAN` / `BLUETOOTH_CONNECT` / `ACCESS_FINE_LOCATION` permission requests automatically.

## Background Monitoring

`BioBeatForegroundService` keeps the BLE connection alive when the app is backgrounded. It runs as a `connectedDevice` foreground service type and updates a persistent notification with the latest heart rate reading.

## Testing

**SDK tests** (29 tests):
- `HeartRateParserTest` — standard BT Heart Rate Measurement parsing
- `EcgParserTest` — custom ECG protocol parsing
- `BatteryParserTest` — battery level parsing and clamping
- `SettingsSerializerTest` — settings round-trip serialization
- `ReconnectionPolicyTest` — exponential backoff and attempt limits

**App tests**:
- `CheckHealthAlertUseCaseTest` — heart rate threshold alert logic

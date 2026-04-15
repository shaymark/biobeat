# BioBeat SDK

Android BLE communication SDK for BioBeat wearable health monitoring devices.

## Overview

The BioBeat SDK handles all Bluetooth Low Energy communication with BioBeat wearable devices. It provides a clean Kotlin coroutines/Flow API for:

- Scanning for nearby devices
- Connecting and managing the BLE lifecycle (with automatic reconnection)
- Streaming live heart rate data (standard Bluetooth Heart Rate profile)
- Streaming live ECG waveform data (~250 Hz)
- Receiving device notifications and alerts
- Reading device status (battery, memory, firmware version)
- Reading and writing device settings

The SDK is stateless and headless — it contains no UI, no database, no network calls, and no dependency injection framework. Your app decides what to do with the data.

## Requirements

- Android API 26+ (Android 8.0)
- Kotlin coroutines
- BLE permissions granted at runtime by the consuming app

## Installation

### Local project dependency

If the SDK module lives in the same Gradle project:

```kotlin
// settings.gradle.kts
include(":biobeat-sdk")

// app/build.gradle.kts
dependencies {
    implementation(project(":biobeat-sdk"))
}
```

### Maven dependency (when published)

```kotlin
dependencies {
    implementation("com.biobeat:sdk:1.0.0")
}
```

## Quick Start

### 1. Initialize the SDK

Call `BioBeatSdk.init()` once, typically in your `Application.onCreate()`. The SDK extracts `applicationContext` internally — no context leaks.

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        BioBeatSdk.init(this)
    }
}
```

With custom configuration:

```kotlin
BioBeatSdk.init(
    context = this,
    config = BioBeatSdkConfig(
        autoReconnect = true,
        reconnectDelayMs = 2_000L,
        maxReconnectAttempts = 5,     // 0 = infinite
        logLevel = BioBeatSdkConfig.LogLevel.DEBUG,
    ),
)
```

### 2. Scan for devices

```kotlin
BioBeatSdk.scan(timeoutMs = 10_000L).collect { device ->
    // device: DeviceInfo(name, macAddress, rssi)
    println("Found: ${device.name} at ${device.macAddress}")
}
```

### 3. Connect to a device

```kotlin
val connection = BioBeatSdk.device("AA:BB:CC:DD:EE:FF")

// connect() suspends until connected or throws on failure
connection.connect()
```

### 4. Stream live heart rate

```kotlin
connection.heartRate.collect { reading ->
    println("HR: ${reading.bpm} BPM, contact: ${reading.sensorContact}")
    reading.rrIntervals.forEach { rr ->
        println("  RR interval: ${rr}ms")
    }
}
```

### 5. Stream live ECG

```kotlin
connection.ecgData.collect { sample ->
    // sample.millivolts is a FloatArray of waveform values
    // sample.sampleRateHz is typically 250
    // sample.sequenceNumber for ordering/gap detection
    updateWaveformChart(sample.millivolts)
}
```

### 6. Observe connection state

```kotlin
connection.connectionState.collect { state ->
    when (state) {
        is ConnectionState.Disconnected -> showDisconnected()
        is ConnectionState.Connecting   -> showConnecting()
        is ConnectionState.Connected    -> showConnected()
        is ConnectionState.Reconnecting -> showReconnecting(state.attempt, state.maxAttempts)
        is ConnectionState.Failed       -> showError(state.error.message)
    }
}
```

### 7. Read device status

```kotlin
// Status is updated automatically on connect and via refreshStatus()
connection.deviceStatus.collect { status ->
    status?.let {
        println("Battery: ${it.batteryPercent}%, charging: ${it.isCharging}")
        println("Memory: ${it.memoryUsedBytes}/${it.memoryTotalBytes} bytes")
        println("Firmware: ${it.firmwareVersion}")
    }
}

// Force a refresh
connection.refreshStatus()
```

### 8. Device notifications

```kotlin
connection.notifications.collect { notification ->
    // notification.type: ALERT, REMINDER, SYSTEM, UNKNOWN
    // notification.severity: LOW, MEDIUM, HIGH, CRITICAL
    println("[${notification.severity}] ${notification.message}")
}
```

### 9. Read and write settings

```kotlin
// Read current settings
val settings = connection.readSettings()
println("ECG enabled: ${settings.ecgEnabled}")
println("HR interval: ${settings.heartRateInterval}s")

// Write new settings
connection.writeSettings(
    settings.copy(
        ecgEnabled = true,
        heartRateInterval = 5,
        notificationsEnabled = true,
        deviceName = "My BioBeat",
    )
)
```

### 10. Disconnect

```kotlin
connection.disconnect()
```

## Full ViewModel Example

```kotlin
@HiltViewModel
class MonitorViewModel @Inject constructor() : ViewModel() {

    private val connection = BioBeatSdk.device("AA:BB:CC:DD:EE:FF")

    val connectionState = connection.connectionState
    val deviceStatus = connection.deviceStatus

    val latestHeartRate: StateFlow<HeartRateReading?> =
        connection.heartRate
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    init {
        viewModelScope.launch {
            connection.connect()
        }
    }

    override fun onCleared() {
        viewModelScope.launch { connection.disconnect() }
    }
}
```

## Public API Reference

### `BioBeatSdk`

| Method | Description |
|--------|-------------|
| `init(context, config)` | Initialize the SDK. Call once in `Application.onCreate()`. |
| `version()` | Returns the SDK version string (semver). |
| `device(macAddress)` | Creates a `DeviceConnection` handle. Connection is not established until `connect()` is called. |
| `scan(timeoutMs)` | Returns a `Flow<DeviceInfo>` of discovered devices. Completes after timeout. |
| `shutdown()` | Releases all SDK resources. `init()` must be called again after this. |

### `DeviceConnection`

| Property/Method | Type | Description |
|-----------------|------|-------------|
| `connectionState` | `StateFlow<ConnectionState>` | Current BLE connection state. |
| `heartRate` | `Flow<HeartRateReading>` | Live heart rate stream. |
| `ecgData` | `Flow<EcgSample>` | Live ECG waveform stream (~250 Hz). |
| `notifications` | `Flow<DeviceNotification>` | Device-pushed alerts and notifications. |
| `deviceStatus` | `StateFlow<DeviceStatus?>` | Battery, memory, firmware info. |
| `connect()` | `suspend` | Establish BLE connection. Suspends until ready. |
| `disconnect()` | `suspend` | Gracefully disconnect and release resources. |
| `readSettings()` | `suspend -> DeviceSettings` | Read current device configuration. |
| `writeSettings(settings)` | `suspend` | Write new configuration to the device. |
| `refreshStatus()` | `suspend` | Force-refresh `deviceStatus`. |

### Data Models

| Model | Key Fields |
|-------|------------|
| `HeartRateReading` | `bpm`, `sensorContact`, `rrIntervals`, `timestampMs` |
| `EcgSample` | `sequenceNumber`, `millivolts: FloatArray`, `sampleRateHz`, `timestampMs` |
| `DeviceStatus` | `batteryPercent`, `isCharging`, `memoryUsedBytes`, `memoryTotalBytes`, `firmwareVersion` |
| `DeviceNotification` | `id`, `type` (ALERT/REMINDER/SYSTEM), `message`, `severity` (LOW/MEDIUM/HIGH/CRITICAL) |
| `DeviceSettings` | `ecgEnabled`, `heartRateInterval`, `notificationsEnabled`, `deviceName` |
| `DeviceInfo` | `name`, `macAddress`, `rssi` |

### Connection States

| State | Meaning |
|-------|---------|
| `Disconnected` | No connection. Initial state. |
| `Connecting` | BLE connection in progress. |
| `Connected` | Connected and ready for data. |
| `Reconnecting(attempt, maxAttempts)` | Connection lost, auto-reconnecting. |
| `Failed(error)` | Terminal failure. |

### Exceptions

All SDK exceptions extend `BioBeatException` (sealed class):

| Exception | When |
|-----------|------|
| `NotInitialized` | `init()` was not called. |
| `AlreadyInitialized` | `init()` called again with a different config. |
| `InvalidAddress` | MAC address format is invalid. |
| `BluetoothDisabled` | Bluetooth adapter is off. |
| `PermissionMissing` | Required BLE permission not granted. |
| `ConnectionFailed` | BLE connection could not be established. |
| `NotConnected` | Operation requires a connection but device is disconnected. |
| `WriteFailed` | GATT write was not acknowledged. |
| `GattError` | GATT operation returned an error status. |

## Android Permissions

The SDK declares the following permissions in its manifest (merged automatically into your app):

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

Your app is responsible for requesting these permissions at runtime before calling `scan()` or `connect()`.

## Threading Model

- All BLE operations run on a dedicated single-threaded dispatcher internally. You do not need to manage BLE threading.
- All public `Flow` emissions are dispatched internally — safe to collect from `Main`, `IO`, or any other dispatcher.
- All `suspend` functions are main-safe.

## ProGuard

The SDK ships consumer ProGuard rules automatically. No additional configuration is needed in your app.

## Dependencies

The SDK has minimal dependencies to avoid conflicts:

- `kotlinx-coroutines-android`
- `androidx-core-ktx`
- `androidx-annotation`

No UI libraries, no DI frameworks, no networking libraries.

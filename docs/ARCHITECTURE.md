# Architecture

This document describes the software architecture of E-Rickshaw Battery Unlock.

## Overview

The application follows **Clean Architecture** with the **MVVM** (Model-View-ViewModel) pattern. The codebase is organized into distinct layers with clear dependency directions:

```
presentation  →  domain  ←  data
```

- **presentation** depends on **domain** (ViewModels call Use Cases)
- **data** depends on **domain** (Repositories implement domain interfaces)
- **domain** depends on nothing (pure Kotlin models, interfaces, and use cases)

## Layers

### domain/

Contains the business logic and core abstractions:

- **`model/`** -- Data classes representing core concepts: `BatteryInfo`, `ConnectionState`, `RecoveryCapability`, `RecoveryResult`, `BatteryDevice`, `BatteryStatus`, `SavedBattery`
- **`repository/`** -- Interfaces that data-layer classes implement: `BatteryRepository`, `SavedBatteryRepository`
- **`usecase/`** -- Single-responsibility business operations: `ScanForBatteriesUseCase`, `ConnectToBatteryUseCase`, `DisconnectUseCase`, `ObserveBatteryInfoUseCase`, `ObserveConnectionStateUseCase`, `ExecuteRecoveryUseCase`, `GetRecoveryCapabilityUseCase`, `SetBatteryPasswordUseCase`, `SaveBatteryUseCase`, `GetSavedBatteriesUseCase`, `DeleteSavedBatteryUseCase`

### data/

Contains all data sources and their implementations:

- **`ble/`** -- Bluetooth Low Energy infrastructure:
  - `BleManager` -- Central singleton managing the GATT connection lifecycle. Serializes all GATT operations via a `Mutex`. Handles connection, MTU negotiation, service discovery, polling, and disconnect.
  - `BleScanner` -- Flow-based wrapper around Android's BLE scanner API. Starts scanning when collected, stops when the collector cancels.
  - `BleGattCallback` -- Adapts Android's `BluetoothGattCallback` into a Kotlin `Channel<GattEvent>` for coroutine-friendly consumption.
  - `model/GattEvent` -- Sealed class representing all possible GATT callback events.
- **`ble/protocol/`** -- BMS protocol implementations:
  - `BmsProtocol` -- Interface that all protocol implementations must satisfy.
  - `BmsProtocolFactory` -- Runtime protocol detection based on discovered GATT service UUIDs.
  - `jbd/JbdBmsProtocol` -- JBD/Xiaoxiang protocol implementation (currently the only supported protocol).
  - `jbd/JbdFrameParser` -- Builds and parses JBD protocol frames.
- **`local/`** -- Local storage:
  - `db/` -- Room database for saved batteries (`AppDatabase`, `SavedBatteryDao`, `SavedBatteryEntity`)
  - `security/` -- Encrypted storage using AndroidX Security Crypto
- **`repository/`** -- Repository implementations (`BatteryRepositoryImpl`, `SavedBatteryRepositoryImpl`)

### presentation/

Contains UI screens and their ViewModels:

- Each screen has three files: `*Screen.kt` (Composable), `*ViewModel.kt` (ViewModel), `*UiState.kt` (state data class)
- ViewModels expose a single `StateFlow<UiState>` and handle user actions
- Composables observe the state flow and render the UI

Screens:
- `scan/` -- BLE device scanning and connection initiation
- `dashboard/` -- Live battery telemetry display
- `recovery/` -- Recovery (discharge enable) operation
- `password/` -- BMS password management
- `saved/` -- Saved batteries history

### di/

Hilt dependency injection modules:
- `BleModule` -- Provides `BluetoothAdapter`, `Channel<GattEvent>`, and `JbdBmsProtocol`
- `DatabaseModule` -- Provides Room database and DAO
- `RepositoryModule` -- Binds repository interfaces to implementations

### ui/

Shared UI components:
- `navigation/` -- `AppNavHost` (Navigation Compose) and `Screen` sealed class
- `theme/` -- Material 3 theme configuration (Color, Shape, Type, Theme)

### core/

Cross-cutting utilities:
- `util/BleConstants` -- BLE UUIDs, protocol constants, frame bytes, timeouts
- `extension/ByteArrayExt`` -- Byte array helper functions

## BLE Communication Flow

```
User taps "Connect"
    → ScanViewModel.connectToDevice()
    → ConnectToBatteryUseCase
    → BleManager.connect(address)
        → BluetoothGatt.connectGatt()
        → Wait for GATT connection
        → Negotiate MTU (256)
        → Clear GATT cache (hidden API)
        → Discover services
        → BmsProtocolFactory.detectProtocol()
        → protocol.detectCapabilities()
        → Start polling (every 5s)
    → Navigation to Dashboard
```

## Protocol Extensibility

Adding a new BMS vendor protocol requires:

1. Create a class implementing `BmsProtocol` with the vendor's GATT service UUIDs
2. Implement `detectCapabilities()`, `readBatteryInfo()`, `enableDischarge()`, and `setPassword()`
3. Add the new protocol instance to `BmsProtocolFactory.registeredProtocols`

No other code changes are required. The factory will automatically detect the new protocol at runtime.

## Thread Safety

- All GATT operations in `BleManager` are serialized through a `Mutex` because Android's GATT API is not thread-safe
- The `Channel<GattEvent>` has unlimited capacity because the BLE binder thread cannot suspend
- ViewModels use `viewModelScope` for coroutine management, ensuring automatic cleanup

# E-Rickshaw Battery Unlock

An open-source Android application that communicates with compatible Bluetooth Low Energy (BLE) Battery Management Systems (BMS) used in e-rickshaws.

<!-- Replace with actual screenshot -->
<p align="center">
  <img src="docs/screenshot-scan.png" alt="Scan Screen" width="280" />
  &nbsp;&nbsp;
  <img src="docs/screenshot-dashboard.png" alt="Dashboard Screen" width="280" />
  &nbsp;&nbsp;
  <img src="docs/screenshot-recovery.png" alt="Recovery Screen" width="280" />
</p>

---

## Table of Contents

- [About](#about)
- [Why This Project Exists](#why-this-project-exists)
- [Features](#features)
- [How It Works](#how-it-works)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Project Structure](#project-structure)
- [Installation](#installation)
- [Building from Source](#building-from-source)
- [Download the Debug APK](#download-the-debug-apk)
- [Permissions](#permissions)
- [Supported Android Versions](#supported-android-versions)
- [Project Status](#project-status)
- [Known Limitations](#known-limitations)
- [Community Testing](#community-testing)
- [How to Report Bugs](#how-to-report-bugs)
- [Contributing](#contributing)
- [Roadmap](#roadmap)
- [License](#license)
- [Acknowledgements](#acknowledgements)

---

## About

E-Rickshaw Battery Unlock is designed to help owners and authorized technicians communicate with compatible BLE BMS devices used in e-rickshaws. The application can read battery telemetry, display live status information, and perform supported recovery operations when the connected BMS protocol and hardware allow.

The project is based on publicly documented BLE communication protocols for compatible BMS devices. It is intended to help people regain access to compatible batteries when they have been improperly locked or disabled through compatible software, while remaining compatible with documented BLE protocol behavior.

## Why This Project Exists

E-rickshaws are widely used across many regions as affordable electric transport. The battery management systems in these vehicles can sometimes enter a locked state that prevents normal operation. Existing tools for managing these BMS devices are often closed-source, difficult to use, or not freely available.

This project provides an open, transparent alternative that anyone can inspect, modify, and improve. The source code is fully available so that the community can verify exactly what commands are being sent to the BMS hardware.

## Features

- **BLE Device Scanning** -- Discover nearby BMS devices using Bluetooth Low Energy
- **Live Battery Dashboard** -- View real-time telemetry including voltage, current, state of charge, temperature, cell voltages, and cycle count
- **Battery Recovery** -- Send supported commands to re-enable discharge on compatible BMS devices that have entered a locked state
- **Password Management** -- Set or remove BMS passwords on compatible devices
- **Saved Batteries** -- Keep a local history of previously connected batteries for quick access
- **Automatic Reconnection** -- Exponential back-off retry logic for unstable BLE connections
- **Capability Detection** -- Dynamically detects what operations the connected BMS supports before offering them in the UI

## How It Works

1. **Scan** -- The app scans for nearby BLE devices. The user selects the battery device to connect to.
2. **Connect** -- The app establishes a GATT connection, negotiates MTU, discovers services, and identifies the BMS protocol.
3. **Dashboard** -- Once connected, the app displays live battery telemetry refreshed every 5 seconds.
4. **Recovery** -- If supported by the hardware, the user can send a command to re-enable the discharge MOSFET.
5. **Password** -- The user can set or remove the BMS password on compatible devices.

The application currently implements the JBD (Jiabaida) / Xiaoxiang BMS protocol, which is used by a wide range of generic Chinese BMS modules. The protocol layer is designed to be extensible -- additional vendor protocols can be added by implementing the `BmsProtocol` interface.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose with Material 3 |
| Architecture | Clean Architecture (MVVM) |
| Dependency Injection | Hilt |
| Local Storage | Room (SQLite) |
| Preferences | DataStore |
| Security | AndroidX Security Crypto |
| BLE | Android BluetoothGatt API |
| Navigation | Navigation Compose |
| Coroutines | Kotlin Coroutines + Flow |
| Build System | Gradle (Kotlin DSL) with AGP 9.2.1 |
| Testing | JUnit 4, MockK, Turbine, Espresso |

## Architecture

The project follows Clean Architecture with clear separation of concerns:

```
data/       BLE communication, protocol implementations, local database, repositories
domain/     Models, repository interfaces, use cases
presentation/  UI screens (Composable), ViewModels, UI state classes
di/          Hilt dependency injection modules
ui/          Theme, navigation, shared composables
core/        Utilities, constants, extensions
```

**Data flow:** UI (Compose) -> ViewModel -> Use Case -> Repository -> BLE Manager -> GATT

Key design decisions:
- The BLE layer uses a shared `Channel<GattEvent>` for asynchronous GATT callback communication
- Protocol implementations are stateless; all connection state lives in `BluetoothGatt`
- The `BmsProtocolFactory` selects the correct protocol at runtime based on discovered GATT services
- All GATT operations are serialized through a `Mutex` to comply with Android's single-threaded GATT requirement

## Project Structure

```
app/src/main/java/com/e_rickshawbatteryunlock/developerasaad/
├── core/
│   ├── extension/          ByteArray extensions
│   └── util/               BleConstants (UUIDs, command codes, frame constants)
├── data/
│   ├── ble/
│   │   ├── model/          GattEvent sealed class
│   │   ├── protocol/
│   │   │   ├── BmsProtocol.kt           Protocol interface
│   │   │   ├── BmsProtocolFactory.kt    Runtime protocol selection
│   │   │   └── jbd/
│   │   │       ├── JbdBmsProtocol.kt    JBD/Xiaoxiang implementation
│   │   │       └── JbdFrameParser.kt    Frame parsing and building
│   │   ├── BleManager.kt               GATT connection lifecycle
│   │   ├── BleScanner.kt               Flow-based BLE scanner
│   │   └── BleGattCallback.kt          GATT callback adapter
│   ├── local/
│   │   ├── db/              Room database, DAO, entity
│   │   └── security/        Encrypted storage
│   └── repository/          Repository implementations
├── di/                      Hilt modules (BleModule, DatabaseModule, RepositoryModule)
├── domain/
│   ├── model/               BatteryInfo, ConnectionState, RecoveryCapability, etc.
│   ├── repository/          Repository interfaces
│   └── usecase/             Use cases for each feature
├── presentation/
│   ├── scan/                Scan screen + ViewModel
│   ├── dashboard/           Dashboard screen + ViewModel
│   ├── recovery/            Recovery screen + ViewModel
│   ├── password/            Password management screen + ViewModel
│   └── saved/               Saved batteries screen + ViewModel
└── ui/
    ├── navigation/          NavHost and Screen definitions
    └── theme/               Material 3 theme (Color, Shape, Type, Theme)
```

## Installation

### Prerequisites

- Android Studio Ladybug (2024.2) or later
- JDK 11 or later
- Android SDK 35
- A physical Android device with BLE support (emulators do not support BLE)

### Building from Source

```bash
# Clone the repository
git clone https://github.com/developerasaad/E-Rickshaw-Battery-Unlock.git

# Open the project in Android Studio, or build from the command line:
./gradlew assembleDebug
```

The debug APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Download the Debug APK

If you prefer not to build the project yourself, the latest Debug APK is available in the [GitHub Releases](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/releases) section.

Download the `.apk` file, transfer it to your Android device, and install it. You may need to enable "Install from unknown sources" in your device settings.

## Permissions

The application requires the following permissions:

| Permission | Purpose | Required on |
|---|---|---|
| `BLUETOOTH_SCAN` | Discover nearby BLE devices | Android 12+ (API 31+) |
| `BLUETOOTH_CONNECT` | Connect to BLE devices | Android 12+ (API 31+) |
| `BLUETOOTH` | Legacy BLE access | Android 11 and below (API 30 and below) |
| `BLUETOOTH_ADMIN` | Legacy BLE administration | Android 11 and below (API 30 and below) |
| `ACCESS_FINE_LOCATION` | Required for BLE scanning | Android 6-11 (API 23-30) |
| `ACCESS_COARSE_LOCATION` | Coarse location fallback | Android 6-11 (API 23-30) |

Location permission is required on Android 6 through 11 because the Android BLE scanning API is categorized as a location-tracking feature at the OS level. On Android 12 and later, the dedicated `BLUETOOTH_SCAN` permission (with `neverForLocation`) replaces this requirement.

The app declares `android.hardware.bluetooth_le` as a required feature, so it will not be available for installation on devices without BLE hardware.

## Supported Android Versions

| Requirement | Value |
|---|---|
| Minimum SDK | 23 (Android 6.0 Marshmallow) |
| Target SDK | 35 (Android 15) |
| Compile SDK | 35 (Android 15) |

The app is tested to work on Android 6.0 through Android 15. Older or newer versions may work but are not explicitly tested.

## Project Status

This application is at **version 1.0** -- the first public release.

The core functionality is implemented and working:
- BLE scanning and connection are stable
- JBD/Xiaoxiang protocol communication is functional
- Dashboard displays live telemetry
- Recovery (discharge enable) command is implemented
- Password management is implemented
- Saved batteries history is functional

The project is ready for community testing and contributions.

## Known Limitations

- **Single protocol supported** -- Only the JBD (Jiabaida) / Xiaoxiang protocol is currently implemented. Other BMS vendors (DALY, ANT, PACE, etc.) are not yet supported. The architecture is designed to make adding new protocols straightforward.
- **Hardware testing scope** -- The application has been developed and tested against a limited set of BMS hardware. Different manufacturers, firmware versions, and hardware revisions may behave differently.
- **BLE variability** -- Bluetooth Low Energy behavior varies significantly across Android device manufacturers and OS versions. Connection stability may differ depending on the device.
- **No background operation** -- The app requires an active foreground connection. BLE operations stop when the app is backgrounded.
- **No OTA firmware updates** -- The app does not support over-the-air firmware updates for BMS devices.

## Community Testing

This project was developed against a limited set of real BMS hardware. Broader testing across different devices, firmware versions, and Android phones is essential to improve compatibility and reliability. Community testing is one of the most valuable contributions you can make.

### What to Test

- Connecting to your BMS device
- Reading battery telemetry (voltage, current, SoC, temperature, cell voltages)
- Recovery (discharge enable) workflow
- Reconnect behavior after connection loss
- Password set and removal
- BLE scanning on different phones
- Permission handling across Android versions
- Error handling and edge cases

### What to Report

When reporting a test result, please include:

- **Phone model** (e.g., Samsung Galaxy S23, Xiaomi Redmi Note 12)
- **Android version** (e.g., Android 14, One UI 6.1)
- **BMS manufacturer** (if known)
- **Battery model** (if known)
- **Firmware version** (if known -- some BMS apps display this)
- **Steps to reproduce** the issue
- **Expected behavior** -- what you thought would happen
- **Actual behavior** -- what actually happened
- **Screenshots** (if the issue is visual)
- **Logs** (if available via `adb logcat`)
- **BLE captures** (if available via nRF Connect or similar tools)

### How to Test

1. Download the Debug APK from [GitHub Releases](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/releases) or build from source
2. Install on your Android device
3. Enable Bluetooth and grant permissions when prompted
4. Scan for your BMS device and connect
5. Test the available operations
6. Report results via [GitHub Issues](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues)

## How to Report Bugs

Please use the [Bug Report](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues/new?template=bug_report.md) issue template when reporting bugs. Include as much detail as possible -- device information, steps to reproduce, and any relevant logs or screenshots.

For general questions or feature requests, use the [Feature Request](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues/new?template=feature_request.md) template.

## Contributing

Contributions are welcome. See [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines on how to contribute.

Areas where contributions are especially valuable:
- Additional BMS protocol implementations (DALY, ANT, PACE, etc.)
- Bug fixes and stability improvements
- UI and accessibility improvements
- Documentation improvements
- Testing on additional hardware

## Roadmap

- [ ] Additional BMS protocol support (DALY, ANT, PACE)
- [ ] Cell voltage balancing visualization
- [ ] Battery history and session logging
- [ ] Export diagnostics data
- [ ] Widget for quick battery status
- [ ] Multi-language support
- [ ] Improved error messages and diagnostic output

See [docs/ROADMAP.md](docs/ROADMAP.md) for the full roadmap with details.

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.

## Acknowledgements

- [JBD/Xiaoxiang BMS Protocol](https://github.com/simat/BatteryMonitor/wiki/Generic-Chinese-Bluetooth-BMS-communication-protocol) -- Protocol documentation
- [BatteryMonitor](https://github.com/simat/BatteryMonitor) -- Reference implementation for Chinese BMS communication
- The open-source Android development community
- All contributors and testers

# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-07-06

### Added

- BLE device scanning with real-time device list
- GATT connection with automatic MTU negotiation
- Service and characteristic discovery with GATT cache refresh
- JBD (Jiabaida) / Xiaoxiang BMS protocol implementation
  - Read basic battery info (voltage, current, SoC, temperature, MOS status)
  - Read individual cell voltages
  - Read cycle count and capacity data
  - Enable discharge MOSFET (recovery/unlock operation)
  - Set and remove BMS password
- Dynamic capability detection per connected device
- Live battery dashboard with telemetry polling every 5 seconds
- Recovery workflow with confirmation dialog
- Password management screen with set and remove functionality
- Saved batteries history with local Room database
- Automatic reconnection with exponential back-off (up to 3 attempts)
- Permission handling for Android 6 through Android 15
- Material 3 theming with edge-to-edge display
- Clean Architecture with MVVM pattern
- Hilt dependency injection
- Unit test infrastructure with JUnit 4, MockK, and Turbine

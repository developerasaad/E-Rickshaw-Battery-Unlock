# Frequently Asked Questions

## General

### What is this application?

E-Rickshaw Battery Unlock is an Android application that communicates with compatible Bluetooth Low Energy (BLE) Battery Management Systems (BMS) used in e-rickshaws. It can read battery telemetry, display live status, and perform supported recovery operations.

### Is this app compatible with my battery?

The app currently supports BMS devices that use the JBD (Jiabaida) / Xiaoxiang protocol. This protocol is used by a wide range of generic Chinese BMS modules. If your BMS advertises the FFF0 GATT service, it is likely compatible.

To check compatibility, you can use [nRF Connect](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp) to scan for your BMS and inspect its GATT services. Look for a service with UUID `0000FF00-0000-1000-8000-00805F9B34FB`.

### Does this app unlock every battery?

No. The application communicates with compatible BLE BMS devices using documented communication protocols. It performs supported recovery operations where the connected hardware and protocol allow. Not all BMS devices support all operations, and the app will indicate when an operation is not supported.

### Is this app safe to use?

The application sends commands that are defined by the BMS protocol specification. It does not modify firmware or bypass hardware-level protections. You should only use this application on batteries you own or are authorized to service.

## Installation

### Where can I download the app?

The latest Debug APK is available in the [GitHub Releases](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/releases) section. You can also build the app from source -- see [BUILDING.md](BUILDING.md) for instructions.

### Why do I need to enable "Install from unknown sources"?

The app is not distributed through the Google Play Store. Android requires you to manually allow installation from outside the Play Store. This is a standard Android security setting.

### Which Android versions are supported?

Android 6.0 (API 23) and later. The app targets Android 15 (API 35).

## Usage

### Why can't the app find my battery?

Common reasons:
- Bluetooth is not enabled on your phone
- The battery is not powered on
- The battery is too far away (try within 1-2 meters)
- The BMS uses a different protocol (the app currently only supports JBD/Xiaoxiang)
- Another app or device is already connected to the BMS

### Why does the connection fail?

- Ensure no other device or app is connected to the same BMS (only one connection is allowed)
- Move closer to the battery
- Restart Bluetooth on your phone
- Restart the app and try again

### Why does the connection drop?

BLE connections can be unstable due to:
- Distance -- move closer to the battery
- Interference -- try a location with less wireless interference
- Phone-specific BLE issues -- some Android devices have known BLE stability issues
- Battery power saving -- some phones aggressively manage BLE connections when the screen is off

### Why can't I use the recovery/unlock feature?

The recovery operation requires the BMS to support MOSFET control commands. Not all BMS devices expose this functionality. The app will indicate if the operation is not supported by the connected hardware.

### What does "No compatible BMS protocol detected" mean?

This means the connected BLE device does not advertise a GATT service that matches any known protocol. The device may:
- Use a different BMS protocol (not yet supported)
- Not be a BMS device at all
- Have a GATT configuration that differs from the expected standard

### Why does the app need location permission?

On Android 6 through 11, the BLE scanning API is categorized as a location-tracking feature at the OS level, so location permission is required even though the app does not use location data. On Android 12 and later, the dedicated `BLUETOOTH_SCAN` permission replaces this requirement.

### Can I use this app on an emulator?

No. Android emulators do not support Bluetooth Low Energy. A physical Android device with BLE hardware is required.

## Technical

### How does the app detect which protocol to use?

After connecting to a BLE device, the app discovers all GATT services and compares their UUIDs against registered protocol implementations. The first matching protocol is used.

### What is the polling interval for battery data?

The app refreshes battery telemetry every 5 seconds while connected.

### How does automatic reconnection work?

When a connection is lost, the app attempts to reconnect up to 3 times with exponential back-off (1 second, 2 seconds, 4 seconds). If all attempts fail, the connection state changes to FAILED and the user is returned to the scan screen.

### Can I add support for a new BMS protocol?

Yes. The architecture is designed to make this straightforward. See [ARCHITECTURE.md](ARCHITECTURE.md) for details on the protocol interface and how to add new implementations.

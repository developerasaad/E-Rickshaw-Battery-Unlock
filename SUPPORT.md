# Support

## Getting Help

If you need help using the application, here are the available resources:

### Documentation

- [README](README.md) -- Project overview, installation, and usage
- [FAQ](docs/FAQ.md) -- Frequently asked questions
- [Building from Source](docs/BUILDING.md) -- Detailed build instructions
- [Protocol Support](docs/PROTOCOL_SUPPORT.md) -- Supported BMS protocols and compatibility

### Bug Reports

If you encounter a bug, please [open a bug report](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues/new?template=bug_report.md) with:

- A clear description of the issue
- Steps to reproduce
- Device model and Android version
- BMS manufacturer and firmware version (if known)
- Screenshots or logs (if applicable)

### Feature Requests

Feature requests can be submitted through the [Feature Request](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues/new?template=feature_request.md) template.

### General Questions

For general questions about the project, open a [GitHub Issue](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues) or start a discussion.

## What to Include When Asking for Help

To help resolve your issue quickly, please include:

- **Device information**: Phone model, Android version, BLE hardware details
- **BMS information**: Manufacturer, model, firmware version (if known)
- **Steps taken**: What you did before the issue occurred
- **Expected behavior**: What you expected to happen
- **Actual behavior**: What actually happened
- **Logs**: If available, include relevant `adb logcat` output
- **Screenshots**: If the issue is visual

## Troubleshooting

### App cannot find my battery

- Ensure Bluetooth is enabled on your phone
- Make sure the battery is powered on and within range (typically 1-5 meters)
- Check that the BMS uses a compatible protocol (currently JBD/Xiaoxiang)
- Try restarting the scan

### Connection fails or drops

- Move closer to the battery
- Restart Bluetooth on your phone
- Restart the app and try again
- Check if another app is connected to the same BMS (only one connection is allowed at a time)

### Recovery command does not work

- Not all BMS devices support all operations
- The app will indicate if an operation is not supported by the connected hardware
- Check the BMS firmware version -- some firmware versions may not support the recovery command
- Ensure the battery has sufficient charge for the BMS to be operational

### Location permission denied (Android 6-11)

- On Android 6 through 11, location permission is required for BLE scanning
- Go to Settings > Apps > E-Rickshaw Battery Unlock > Permissions > Location and grant the permission
- On Android 12 and later, this is not required -- the app uses the dedicated Bluetooth permissions instead

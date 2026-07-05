# Contributing to E-Rickshaw Battery Unlock

Thank you for your interest in contributing. This project exists because of community effort, and every contribution helps improve compatibility and reliability for e-rickshaw owners and technicians.

## How to Contribute

### Reporting Bugs

If you encounter a bug, please [open an issue](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues/new?template=bug_report.md) with:

- A clear description of the problem
- Steps to reproduce
- Your device model and Android version
- BMS manufacturer and firmware version (if known)
- Relevant logs (`adb logcat`) or screenshots

### Suggesting Features

Feature requests are welcome. [Open a feature request](https://github.com/developerasaad/E-Rickshaw-Battery-Unlock/issues/new?template=feature_request.md) describing the problem you are trying to solve and your proposed solution.

### Testing on Hardware

One of the most valuable contributions is testing the application on real hardware that the maintainers do not have access to. See the [Community Testing](README.md#community-testing) section in the README for details on what to test and how to report results.

### Submitting Code

#### Getting Started

1. Fork the repository
2. Clone your fork locally
3. Create a new branch from `main` for your changes
4. Make your changes
5. Test thoroughly on a real device
6. Commit with a clear message
7. Push to your fork and open a Pull Request

#### Development Setup

**Prerequisites:**
- Android Studio Ladybug (2024.2) or later
- JDK 11+
- Android SDK 35
- A physical Android device with BLE

**Building:**

```bash
git clone https://github.com/your-username/E-Rickshaw-Battery-Unlock.git
cd E-Rickshaw-Battery-Unlock
./gradlew assembleDebug
```

Install the debug APK on your device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

#### Code Style

- Follow the existing code style and conventions in the project
- Use Kotlin idioms and standard library functions where appropriate
- Keep functions focused and small
- Name variables and functions clearly -- the code should be self-documenting
- Minimize comments; only add them when the reasoning is non-obvious

#### Commit Messages

Write clear, concise commit messages:

- Use the imperative mood ("Add feature" not "Added feature")
- Keep the first line under 72 characters
- Reference issue numbers where relevant (e.g., "Fix reconnect crash on Android 14, closes #12")

#### Pull Request Guidelines

- Describe what the PR does and why
- Reference any related issues
- Include screenshots or logs if the change affects the UI or BLE behavior
- Test on a real device before submitting
- Keep PRs focused -- one logical change per PR
- Ensure the project builds without errors (`./gradlew assembleDebug`)

### Improving Documentation

Documentation improvements are always welcome. This includes:

- Fixing typos or unclear wording
- Adding usage examples
- Improving setup instructions
- Translating documentation
- Adding or improving inline code documentation

### Adding BMS Protocol Support

The architecture is designed to make adding new protocols straightforward:

1. Create a new class implementing `BmsProtocol` in `data/ble/protocol/`
2. Define the GATT service UUIDs for the new BMS vendor
3. Implement frame parsing, capability detection, and command building
4. Register the new protocol in `BmsProtocolFactory`
5. Test on real hardware

If you add a new protocol, please include:
- The protocol specification or reference documentation
- Test results from real hardware
- Any known limitations or quirks

## Code of Conduct

This project follows a Code of Conduct. See [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md). By participating, you agree to uphold its standards.

## Questions?

If you have questions about contributing, feel free to open an issue or reach out through GitHub Discussions.

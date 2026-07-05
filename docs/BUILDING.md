# Building from Source

## Prerequisites

| Requirement | Version |
|---|---|
| Android Studio | Ladybug (2024.2) or later |
| JDK | 11 or later |
| Android SDK | 35 (API level 35) |
| Android Build Tools | 35.0.0 or later |
| Gradle | 9.4.1 (bundled with wrapper) |

**Note:** A physical Android device with BLE support is required for testing. Android emulators do not support Bluetooth Low Energy.

## Clone and Build

```bash
# Clone the repository
git clone https://github.com/developerasaad/E-Rickshaw-Battery-Unlock.git
cd E-Rickshaw-Battery-Unlock

# Build the debug APK
./gradlew assembleDebug
```

The debug APK will be generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Build Variants

| Variant | Description |
|---|---|
| `debug` | Development build with debug signing. Includes Compose preview tooling and test manifest. |
| `release` | Production build. Requires signing configuration (not included in repository). |

## Install on Device

### Using ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

### Manual Transfer

1. Copy the APK file to your device (via USB, cloud storage, or file sharing)
2. Open the file on your device
3. Allow installation from unknown sources if prompted
4. Follow the installation prompts

## Build Configuration

### Gradle

The project uses Gradle with Kotlin DSL and the Configuration Cache enabled.

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
```

### Version Catalog

Dependencies are managed through a Gradle Version Catalog at `gradle/libs.versions.toml`. To update a dependency version, edit the corresponding entry in the `[versions]` section.

### AGP 9 Notes

This project uses Android Gradle Plugin 9.2.1, which has built-in Kotlin support. The `kotlin.android` plugin must NOT be applied -- AGP 9 provides this automatically. The Compose compiler plugin (`kotlin.plugin.compose`) is applied separately.

## Running Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires connected device or emulator)
./gradlew connectedDebugAndroidTest
```

## Troubleshooting

### "Cannot add extension with name 'kotlin'"

This error occurs if `kotlin.android` is applied alongside AGP 9. Ensure the `build.gradle.kts` files do NOT contain `alias(libs.plugins.kotlin.android)`.

### "Duplicate META-INF license files"

The project already excludes duplicate META-INF files in `app/build.gradle.kts`. If new duplicates appear, add the file pattern to the `packaging.resources.excludes` list.

### "KSP source set conflict"

AGP 9's built-in Kotlin may conflict with KSP's source set registration. The `android.disallowKotlinSourceSets=false` flag in `gradle.properties` addresses this.

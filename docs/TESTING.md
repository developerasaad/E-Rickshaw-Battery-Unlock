# Testing

This document describes the testing approach and how to run tests for the project.

## Test Types

### Unit Tests

Unit tests verify individual components in isolation using mocks. The project uses:

- **JUnit 4** -- Test runner and assertions
- **MockK** -- Mocking framework for Kotlin
- **Turbine** -- Flow testing library
- **kotlinx-coroutines-test** -- Coroutine test utilities

Located in: `app/src/test/java/com/e_rickshawbatteryunlock/developerasaad/`

### Instrumented Tests

Instrumented tests run on a real device or emulator. The project uses:

- **AndroidX Test** -- Core test libraries
- **Espresso** -- UI testing framework
- **Compose UI Test** -- Compose-specific test utilities
- **MockK Android** -- Mocking for Android components

Located in: `app/src/androidTest/java/com/e_rickshawbatteryunlock/developerasaad/`

## Running Tests

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run a specific test class
./gradlew testDebugUnitTest --tests "com.e_rickshawbatteryunlock.developerasaad.ExampleUnitTest"

# Run all instrumented tests (requires connected device)
./gradlew connectedDebugAndroidTest

# Run instrumented tests on a specific device
./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.device=<device-serial>
```

## Writing Unit Tests

### ViewModel Tests

ViewModel tests verify state transitions and side effects:

```kotlin
@Test
fun `when scan starts, ui state updates to scanning`() = runTest {
    // Given
    val viewModel = ScanViewModel(scanForBatteries, connectToBattery, saveBattery, bluetoothAdapter)

    // When
    viewModel.startScan()

    // Then
    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue(state.isScanning)
    }
}
```

### Use Case Tests

Use case tests verify business logic with mocked repositories:

```kotlin
@Test
fun `connect to battery returns success when repository succeeds`() = runTest {
    coEvery { batteryRepository.connect(any()) } returns Result.success(Unit)
    val useCase = ConnectToBatteryUseCase(batteryRepository)
    
    val result = useCase(BatteryDevice("Test", "00:11:22:33:44:55", -50))
    
    assertTrue(result.isSuccess)
}
```

### Flow Tests with Turbine

Turbine simplifies testing Kotlin Flows:

```kotlin
@Test
fun `battery info flow emits updates`() = runTest {
    batteryInfoFlow.test {
        val initial = awaitItem()
        assertNull(initial.packVoltageMillivolts)
        // ... trigger update ...
        val updated = awaitItem()
        assertNotNull(updated.packVoltageMillivolts)
    }
}
```

## Test Coverage

Currently the project has:
- Example unit test template
- Example instrumented test template
- Test infrastructure configured (MockK, Turbine, Coroutines Test)

The test infrastructure is in place and ready for comprehensive test coverage. Contributions to improve test coverage are welcome, particularly for:
- `BleManager` connection logic (mocking GATT)
- `JbdFrameParser` frame parsing and building
- ViewModel state management
- Use case logic
- `BmsProtocolFactory` protocol detection

## BLE Testing

BLE operations cannot be unit-tested easily because they depend on real Bluetooth hardware. For BLE-related testing:

1. Use a real device with a compatible BMS
2. Use [nRF Connect](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp) to inspect GATT services
3. Capture BLE traffic for debugging
4. Test connection, reconnection, and error scenarios manually

## Testing Checklist for Contributors

Before submitting a PR that touches BLE or protocol code:

- [ ] Unit tests pass: `./gradlew testDebugUnitTest`
- [ ] Instrumented tests pass: `./gradlew connectedDebugAndroidTest`
- [ ] Manual test on real device: scan, connect, read data
- [ ] Manual test: recovery operation (if applicable)
- [ ] Manual test: disconnect and reconnect
- [ ] Verify no crashes on connection failure scenarios

# Protocol Support

This document describes the BLE BMS protocols supported by the application and their current implementation status.

## Currently Supported Protocols

### JBD (Jiabaida) / Xiaoxiang

**Status:** Fully implemented

**Vendor names:** JBD, Xiaoxiang, LLT Power, Overkill Solar, and generic Chinese BMS modules using the FFF0 service

**GATT Service UUID:** `0000FF00-0000-1000-8000-00805F9B34FB` (FFF0)

**Characteristics:**
| UUID | Property | Purpose |
|---|---|---|
| `0000FF01-...` (FFF1) | Notify | BMS response frames |
| `0000FF02-...` (FFF2) | Write (No Response) | Command frames |

**Supported operations:**
- Read basic battery info (voltage, current, SoC, temperature, MOS status)
- Read individual cell voltages
- Enable/disable discharge MOSFET
- Enable/disable charge MOSFET
- Set and remove BMS password

**Frame format:**
```
Request:  0xDD 0xA5 <cmd> 0x00 <checksum_hi> <checksum_lo> 0x77
Response: 0xDD 0x<cmd> 0x00 <len> <data...> <checksum_hi> <checksum_lo> 0x77
Error:    0xDD 0x<cmd> 0x80 <len> <error_code> <checksum_hi> <checksum_lo> 0x77
```

**Protocol references:**
- [Generic Chinese BMS Communication Protocol](https://github.com/simat/BatteryMonitor/wiki/Generic-Chinese-Bluetooth-BMS-communication-protocol)
- [JBD Protocol Documentation (FurTrader/OverkillSolarBMS)](https://github.com/OverkillSolarBMS/BatteryMonitor)
- [smart_bms (KrystianD)](https://github.com/KrystianD/smart_bms)
- [GenericBMSArduino (ForrestFire0)](https://github.com/ForrestFire0/GenericBMSArduino)

**Known quirks:**
- Many JBD clones do not expose the CCCD descriptor by standard UUID. The app falls back to using the first available descriptor.
- The write characteristic only supports WRITE_NO_RESPONSE on most hardware. The app detects this from the characteristic properties.
- Some firmware versions require the GATT cache to be cleared before service discovery works correctly. The app calls `BluetoothGatt.refresh()` (hidden API) before discovery.
- Large responses may be fragmented across multiple BLE notifications. The app accumulates notification bytes until a complete frame is received.

## Planned Protocols

These protocols are not yet implemented but are planned for future development:

| Protocol | Status | Notes |
|---|---|---|
| DALY BMS | Planned | Common in e-rickshaw applications |
| ANT BMS | Planned | Used in some high-power applications |
| PACE BMS | Planned | Less common but growing in adoption |
| JKBMS | Planned | Popular in DIY battery projects |

## Adding New Protocol Support

The architecture is designed to make adding new protocols straightforward. See [ARCHITECTURE.md](ARCHITECTURE.md) for details on the protocol interface and factory pattern.

Steps to add a new protocol:

1. Research the target BMS protocol (GATT UUIDs, command format, frame structure)
2. Create a new class implementing `BmsProtocol`
3. Implement frame parsing in a companion parser class
4. Add the protocol to `BmsProtocolFactory.registeredProtocols`
5. Test on real hardware
6. Document any quirks or limitations

## Protocol Detection

The application detects protocols at runtime through GATT service discovery:

1. Connect to the BLE device
2. Discover all GATT services and characteristics
3. Compare discovered service UUIDs against registered protocols
4. Use the first matching protocol

If no protocol matches, the application reports "No compatible BMS protocol detected."

## Compatibility Notes

- The application requires the BMS to expose a GATT service with known UUIDs
- The BMS must respond to standard GATT read/write operations
- The BMS firmware must implement the expected command set
- Some BMS devices may require a password before accepting certain commands
- The application does not modify firmware or bypass hardware-level protections

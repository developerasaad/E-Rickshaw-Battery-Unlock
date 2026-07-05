package com.e_rickshawbatteryunlock.developerasaad.data.ble.model

/**
 * Sealed hierarchy of events emitted from [BleGattCallback].
 *
 * Every [android.bluetooth.BluetoothGattCallback] method maps to exactly one
 * event subtype. The [BleManager] consumes these events from a Channel and
 * processes them sequentially in its coroutine.
 */
sealed class GattEvent {

    /** GATT connection state changed. */
    data class ConnectionStateChanged(val newState: Int, val status: Int) : GattEvent()

    /** GATT services discovered after a successful connection. */
    data class ServicesDiscovered(val status: Int) : GattEvent()

    /** A characteristic read operation completed. */
    data class CharacteristicRead(
        val uuid: java.util.UUID,
        val value: ByteArray,
        val status: Int,
    ) : GattEvent()

    /** A characteristic write operation completed. */
    data class CharacteristicWrite(
        val uuid: java.util.UUID,
        val status: Int,
    ) : GattEvent()

    /** A characteristic notification or indication was received. */
    data class CharacteristicChanged(
        val uuid: java.util.UUID,
        val value: ByteArray,
    ) : GattEvent()

    /** Descriptor write completed (used for CCCD to enable notifications). */
    data class DescriptorWrite(
        val uuid: java.util.UUID,
        val status: Int,
    ) : GattEvent()

    /** MTU change request completed. */
    data class MtuChanged(val mtu: Int, val status: Int) : GattEvent()
}

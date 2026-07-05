package com.e_rickshawbatteryunlock.developerasaad.data.ble

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.os.Build
import com.e_rickshawbatteryunlock.developerasaad.data.ble.model.GattEvent
import kotlinx.coroutines.channels.Channel

/**
 * Bridges the [BluetoothGattCallback] callback API to a [Channel]-based event stream.
 *
 * Android's GATT callbacks are delivered on a binder thread. Sending to a Channel
 * is thread-safe and allows [BleManager] to process events sequentially on a single
 * coroutine without synchronization complexity.
 *
 * [Channel.UNLIMITED] capacity prevents any callback from ever blocking the binder
 * thread, which would cause a system-level ANR risk.
 *
 * @param eventChannel The channel that receives all GATT events.
 */
internal class BleGattCallback(
    private val eventChannel: Channel<GattEvent>,
) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
        eventChannel.trySend(GattEvent.ConnectionStateChanged(newState, status))
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
        eventChannel.trySend(GattEvent.ServicesDiscovered(status))
    }

    @Suppress("DEPRECATION")
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        // Android 12 and below — value is in the characteristic object
        val value = characteristic.value ?: byteArrayOf()
        eventChannel.trySend(
            GattEvent.CharacteristicRead(
                uuid = characteristic.uuid,
                value = value,
                status = status,
            )
        )
    }

    // Android 13+ (API 33) provides value directly in the callback
    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int,
    ) {
        eventChannel.trySend(
            GattEvent.CharacteristicRead(
                uuid = characteristic.uuid,
                value = value,
                status = status,
            )
        )
    }

    override fun onCharacteristicWrite(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        status: Int,
    ) {
        eventChannel.trySend(
            GattEvent.CharacteristicWrite(
                uuid = characteristic.uuid,
                status = status,
            )
        )
    }

    @Suppress("DEPRECATION")
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
    ) {
        // Android 12 and below
        val value = characteristic.value ?: byteArrayOf()
        eventChannel.trySend(
            GattEvent.CharacteristicChanged(
                uuid = characteristic.uuid,
                value = value,
            )
        )
    }

    // Android 13+ (API 33)
    override fun onCharacteristicChanged(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
    ) {
        eventChannel.trySend(
            GattEvent.CharacteristicChanged(
                uuid = characteristic.uuid,
                value = value,
            )
        )
    }

    override fun onDescriptorWrite(
        gatt: BluetoothGatt,
        descriptor: BluetoothGattDescriptor,
        status: Int,
    ) {
        eventChannel.trySend(
            GattEvent.DescriptorWrite(
                uuid = descriptor.uuid,
                status = status,
            )
        )
    }

    override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
        eventChannel.trySend(GattEvent.MtuChanged(mtu, status))
    }
}

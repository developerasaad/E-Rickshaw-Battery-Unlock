package com.e_rickshawbatteryunlock.developerasaad.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import com.e_rickshawbatteryunlock.developerasaad.data.ble.model.GattEvent
import com.e_rickshawbatteryunlock.developerasaad.data.ble.protocol.jbd.JbdBmsProtocol
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.channels.Channel
import javax.inject.Singleton

/**
 * Hilt module providing BLE infrastructure bindings.
 *
 * [BluetoothAdapter] and the shared [Channel] are singletons because they
 * represent hardware resources that should not be duplicated.
 */
@Module
@InstallIn(SingletonComponent::class)
object BleModule {

    /**
     * Provides the system [BluetoothAdapter].
     *
     * Note: On some Android versions, [BluetoothManager.getAdapter] may return null
     * if the device does not have Bluetooth hardware. The app will crash at injection
     * time with a clear NullPointerException. This is acceptable — without Bluetooth
     * the app cannot function, and the `<uses-feature>` manifest declaration will
     * already prevent installation on non-BLE devices.
     */
    @Provides
    @Singleton
    fun provideBluetoothAdapter(
        @ApplicationContext context: Context,
    ): BluetoothAdapter {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter
    }

    /**
     * Provides the shared GATT event channel used by [BleGattCallback] and [JbdBmsProtocol].
     *
     * UNLIMITED capacity is critical — the BLE binder thread cannot suspend,
     * so [Channel.trySend] is used in callbacks and must never drop events.
     */
    @Provides
    @Singleton
    fun provideGattEventChannel(): Channel<GattEvent> = Channel(Channel.UNLIMITED)

    /**
     * Provides the [JbdBmsProtocol] with the shared event channel injected.
     *
     * The protocol is a singleton because it holds no per-connection state —
     * all state is carried by the [BluetoothGatt] instance passed to each method.
     */
    @Provides
    @Singleton
    fun provideJbdBmsProtocol(
        eventChannel: Channel<GattEvent>,
    ): JbdBmsProtocol = JbdBmsProtocol(eventChannel)
}

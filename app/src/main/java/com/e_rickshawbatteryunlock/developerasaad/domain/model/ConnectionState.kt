package com.e_rickshawbatteryunlock.developerasaad.domain.model

/**
 * Represents the GATT connection lifecycle state.
 *
 * States transition linearly in the happy path:
 *   DISCONNECTED → CONNECTING → DISCOVERING_SERVICES → CONNECTED
 *
 * From any state, a failure or user-initiated disconnect leads to DISCONNECTED.
 */
enum class ConnectionState {
    /** No active BLE connection. */
    DISCONNECTED,

    /** GATT connection request sent; awaiting onConnectionStateChange callback. */
    CONNECTING,

    /** GATT connected; discovering services and characteristics. */
    DISCOVERING_SERVICES,

    /** Connection established and services discovered. Ready for operations. */
    CONNECTED,

    /** Connection failed with an unrecoverable error. App should show error and reset. */
    FAILED,
}

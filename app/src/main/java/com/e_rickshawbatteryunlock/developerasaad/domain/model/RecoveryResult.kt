package com.e_rickshawbatteryunlock.developerasaad.domain.model

/**
 * The outcome of a recovery (discharge enable) operation.
 *
 * The UI renders each case differently:
 *  - [Success]     → Green checkmark + "Battery unlocked successfully"
 *  - [Failure]     → Red X + human-readable reason + Retry button
 *  - [Unsupported] → Amber info card + explanation why it cannot be done
 */
sealed class RecoveryResult {

    /**
     * The recovery command was written successfully and the BMS confirmed
     * the discharge switch is now enabled.
     */
    data object Success : RecoveryResult()

    /**
     * The recovery command was sent but the operation failed.
     *
     * @param reason A human-readable explanation of the failure.
     *               Must not contain a stack trace or technical jargon.
     */
    data class Failure(val reason: String) : RecoveryResult()

    /**
     * The connected BMS does not support this recovery operation.
     *
     * This happens when the required GATT characteristic is not present
     * on the device — the operation is not attempted.
     *
     * @param explanation Why this operation is not supported on this device.
     */
    data class Unsupported(val explanation: String) : RecoveryResult()
}

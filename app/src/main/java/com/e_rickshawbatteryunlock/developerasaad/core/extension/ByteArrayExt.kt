package com.e_rickshawbatteryunlock.developerasaad.core.extension

/**
 * Returns the unsigned integer value of a [Byte].
 *
 * Kotlin's Byte is signed (-128..127). BMS protocol bytes are always
 * interpreted as unsigned (0..255). This extension prevents accidental
 * sign-extension bugs when parsing protocol frames.
 */
fun Byte.toUnsignedInt(): Int = this.toInt() and 0xFF

/**
 * Reads a big-endian signed 16-bit integer from two bytes at [offset].
 *
 * Used for current, voltage, and temperature fields in the JBD protocol
 * which are 16-bit big-endian signed integers.
 */
fun ByteArray.readInt16BE(offset: Int): Int {
    require(offset + 1 < size) { "Offset $offset out of bounds for array of size $size" }
    return (this[offset].toUnsignedInt() shl 8) or this[offset + 1].toUnsignedInt()
}

/**
 * Reads a big-endian unsigned 16-bit integer from two bytes at [offset].
 *
 * Used for voltage and capacity fields that are never negative.
 */
fun ByteArray.readUInt16BE(offset: Int): Int {
    require(offset + 1 < size) { "Offset $offset out of bounds for array of size $size" }
    return (this[offset].toUnsignedInt() shl 8) or this[offset + 1].toUnsignedInt()
}

/**
 * Reads a big-endian signed 16-bit integer treating it as a signed value.
 *
 * Used for current which can be negative (discharging).
 */
fun ByteArray.readSignedInt16BE(offset: Int): Int {
    val raw = readUInt16BE(offset)
    // Convert to signed: if bit 15 is set, the value is negative
    return if (raw and 0x8000 != 0) raw - 0x10000 else raw
}

/**
 * Computes the JBD checksum for a byte array.
 *
 * The JBD checksum is defined as: 0x10000 - sum(data bytes)
 * The result is a 16-bit value where the high byte comes first.
 *
 * @return A [ByteArray] of exactly 2 bytes: [checksum_hi, checksum_lo]
 */
fun ByteArray.jbdChecksum(): ByteArray {
    val sum = fold(0) { acc, byte -> acc + byte.toUnsignedInt() }
    val checksum = (0x10000 - sum) and 0xFFFF
    return byteArrayOf(
        (checksum shr 8).toByte(),
        (checksum and 0xFF).toByte()
    )
}

/**
 * Returns a hex string representation of this byte array for logging.
 *
 * Example: [0xDD, 0xA5, 0x03] → "DD A5 03"
 */
fun ByteArray.toHexString(): String = joinToString(separator = " ") { "%02X".format(it) }

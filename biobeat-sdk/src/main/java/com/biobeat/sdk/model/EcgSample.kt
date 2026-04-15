package com.biobeat.sdk.model

/**
 * A batch of ECG waveform samples from the device.
 *
 * The device sends samples at [sampleRateHz] (typically 250 Hz).
 * Each BLE notification may contain multiple samples in [millivolts].
 *
 * @property sequenceNumber Monotonically increasing sequence for ordering/gap detection.
 * @property millivolts Array of voltage samples in millivolts.
 * @property sampleRateHz Sample rate in Hz (e.g., 250).
 * @property timestampMs Reception timestamp ([System.currentTimeMillis]).
 */
public data class EcgSample(
    val sequenceNumber: Long,
    val millivolts: FloatArray,
    val sampleRateHz: Int,
    val timestampMs: Long,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EcgSample) return false
        return sequenceNumber == other.sequenceNumber &&
            millivolts.contentEquals(other.millivolts) &&
            sampleRateHz == other.sampleRateHz &&
            timestampMs == other.timestampMs
    }

    override fun hashCode(): Int {
        var result = sequenceNumber.hashCode()
        result = 31 * result + millivolts.contentHashCode()
        result = 31 * result + sampleRateHz
        result = 31 * result + timestampMs.hashCode()
        return result
    }
}

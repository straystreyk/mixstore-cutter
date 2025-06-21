package com.example.testandroidapp.plotter

data class Received(
    val type: Int = 0,
    val received: Boolean = false,
    val size: Int = 0,
    val buffer: ByteArray? = null,
    val readData: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Received

        if (type != other.type) return false
        if (received != other.received) return false
        if (size != other.size) return false
        if (buffer != null) {
            if (other.buffer == null) return false
            if (!buffer.contentEquals(other.buffer)) return false
        } else if (other.buffer != null) return false
        if (readData != other.readData) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type
        result = 31 * result + received.hashCode()
        result = 31 * result + size
        result = 31 * result + (buffer?.contentHashCode() ?: 0)
        result = 31 * result + (readData?.hashCode() ?: 0)
        return result
    }
}
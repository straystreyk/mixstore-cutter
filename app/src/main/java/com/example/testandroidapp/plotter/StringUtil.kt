package com.example.testandroidapp.plotter

object StringUtil {
    fun toHexString(bytes: ByteArray, length: Int): String {
        return bytes.take(length).joinToString(" ") { "%02X".format(it) }
    }

    fun hexStringToString(hex: String): String {
        return try {
            val cleanHex = hex.replace(" ", "")
            val bytes = cleanHex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            hex
        }
    }
}

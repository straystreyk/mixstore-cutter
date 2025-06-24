package com.pokrikinc.mixpokrikcutter.plotter

class DeviceManager private constructor() {
    companion object {
        @Volatile
        private var instance: DeviceManager? = null

        fun getInstance(): DeviceManager {
            return instance ?: synchronized(this) {
                instance ?: DeviceManager().also { instance = it }
            }
        }
    }

    interface Callback {
        fun data(success: Boolean, received: Received?)
    }

    val t485 = T485Service()

    fun start485(): Boolean {
        return start485("/dev/ttyS1", "115200")
    }

    fun start485(devicePath: String, baudRate: String): Boolean {
        t485.close()
        return t485.start(devicePath, baudRate)
    }

    fun destroy() {
        t485.close()
    }

    fun send(data: ByteArray, callback: Callback) {
        try {
            if (t485.driverOpen) {
                val response = t485.callBack(data)
                if (response.type == 1 && !response.readData.isNullOrEmpty()) {
                    callback.data(true, response)
                } else {
                    callback.data(false, response)
                }
            } else {
                callback.data(false, null)
            }
        } catch (e: Exception) {
            callback.data(false, null)
            e.printStackTrace()
        }
    }

    fun send(data: String, callback: Callback) {
        send(data.toByteArray(), callback)
    }

    fun sendNoBack(data: ByteArray, callback: Callback) {
        try {
            if (t485.driverOpen) {
                callback.data(true, t485.notCallBack(data))
            } else {
                callback.data(false, null)
            }
        } catch (e: Exception) {
            callback.data(false, null)
            e.printStackTrace()
        }
    }

    fun sendNoBack(data: String, callback: Callback) {
        sendNoBack(data.toByteArray(), callback)
    }
}
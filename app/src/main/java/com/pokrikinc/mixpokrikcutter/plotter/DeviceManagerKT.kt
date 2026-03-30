package com.pokrikinc.mixpokrikcutter.plotter

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class PrintResult(
    val isSuccess: Boolean,
    val message: String
)

fun DeviceManager.printFile(plt: String) {
    require(plt.isNotEmpty()) { "PLT file path cannot be empty" }

    if (!t485.driverOpen) {
        return
    }

    send(PrintUtil.getState(), object : DeviceManager.Callback {
        override fun data(success: Boolean, received: Received?) {
            if (!success || received == null) {
                LogUtils.d("[Debug]", "Failed to get device state")
                return
            }

            handleStateResponse(this@printFile, plt, received) { }
        }
    })
}

suspend fun DeviceManager.printFileAwait(plt: String): PrintResult {
    require(plt.isNotEmpty()) { "PLT file path cannot be empty" }

    if (!t485.driverOpen) {
        return PrintResult(false, "Driver is closed")
    }

    return suspendCancellableCoroutine { continuation ->
        send(PrintUtil.getState(), object : DeviceManager.Callback {
            override fun data(success: Boolean, received: Received?) {
                if (!success || received == null) {
                    LogUtils.d("[Debug]", "Failed to get device state")
                    if (continuation.isActive) {
                        continuation.resume(PrintResult(false, "Failed to get device state"))
                    }
                    return
                }

                handleStateResponse(this@printFileAwait, plt, received) { result ->
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }
            }
        })
    }
}

private fun handleStateResponse(
    device: DeviceManager,
    plt: String,
    received: Received,
    onResult: (PrintResult) -> Unit
) {
    try {
        val readData = received.readData ?: ""
        val response = StringUtil.hexStringToString(readData)
        LogUtils.d("handleStateResponse", "State response: $response")

        val handshakeValue = response.split(";")
            .firstOrNull { it.contains("RCMD=11") }
            ?.split("=")?.get(1)
            ?.split(",")?.get(1)
            ?.toLongOrNull()
            ?: throw IllegalStateException("Invalid handshake response format")

        LogUtils.d("handleStateResponse", "Handshake value: $handshakeValue")

        device.send(PrintUtil.getHandshake(handshakeValue), object : DeviceManager.Callback {
            override fun data(success: Boolean, received: Received?) {
                if (!success || received == null) {
                    LogUtils.d("handleStateResponse", "Handshake failed")
                    onResult(PrintResult(false, "Handshake failed"))
                    return
                }

                handleHandshakeResponse(device, plt, received, onResult)
            }
        })
    } catch (e: Exception) {
        LogUtils.d("handleStateResponse", "Error processing state response")
        onResult(PrintResult(false, "Error processing state response"))
    }
}

private fun handleHandshakeResponse(
    device: DeviceManager,
    plt: String,
    received: Received,
    onResult: (PrintResult) -> Unit
) {
    try {
        val readData = received.readData ?: ""
        val response = StringUtil.hexStringToString(readData)
        LogUtils.d("handleHandshakeResponse", "Handshake response: $response")

        if (response.contains("RCMD=12")) {
            device.t485.isPrint = true
            device.sendNoBack(PrintUtil.printFile(plt), object : DeviceManager.Callback {
                override fun data(success: Boolean, received: Received?) {
                    val result = if (success) {
                        LogUtils.d("handleHandshakeResponse", "File sent successfully")
                        PrintResult(true, "File sent successfully")
                    } else {
                        LogUtils.d("handleHandshakeResponse", "Failed to send file")
                        PrintResult(false, "Failed to send file")
                    }
                    onResult(result)
                }
            })
        } else {
            LogUtils.e("handleHandshakeResponse", "Unexpected handshake response: $response")
            onResult(PrintResult(false, "Unexpected handshake response"))
        }
    }
    catch (e: Exception) {
        LogUtils.e("handleHandshakeResponse", "Error processing handshake response")
        onResult(PrintResult(false, "Error processing handshake response"))
    }
}

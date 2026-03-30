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
        LogUtils.e("PlotterPrint", "printFile aborted: driver closed")
        return
    }

    send(PrintUtil.getState(), object : DeviceManager.Callback {
        override fun data(success: Boolean, received: Received?) {
            val readData = received?.readData
            LogUtils.d(
                "PlotterPrint",
                "legacy getState callback success=$success received=${readData?.take(120)}"
            )
            if (readData.isNullOrEmpty()) {
                LogUtils.e("PlotterPrint", "legacy getState failed: empty response")
                return
            }

            handleLegacyStateResponse(this@printFile, plt, readData) { }
        }
    })
}

suspend fun DeviceManager.printFileAwait(plt: String): PrintResult {
    require(plt.isNotEmpty()) { "PLT file path cannot be empty" }

    if (!t485.driverOpen) {
        LogUtils.e("PlotterPrint", "printFileAwait failed: driver closed")
        return PrintResult(false, "Driver is closed")
    }

    return suspendCancellableCoroutine { continuation ->
        send(PrintUtil.getState(), object : DeviceManager.Callback {
            override fun data(success: Boolean, received: Received?) {
                val readData = received?.readData
                LogUtils.d(
                    "PlotterPrint",
                    "legacy printFileAwait getState success=$success received=${readData?.take(120)}"
                )
                if (readData.isNullOrEmpty()) {
                    if (continuation.isActive) {
                        continuation.resume(PrintResult(false, "Failed to get device state"))
                    }
                    return
                }

                handleLegacyStateResponse(this@printFileAwait, plt, readData) { result ->
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }
            }
        })
    }
}

private fun handleLegacyStateResponse(
    device: DeviceManager,
    plt: String,
    readData: String,
    onResult: (PrintResult) -> Unit
) {
    try {
        val response = StringUtil.hexStringToString(readData)
        LogUtils.d("PlotterPrint", "legacy state response=$response")
        val stateChunk = response.split(";").first { it.contains("RCMD=11") }
        val handshakeValue = stateChunk
            .split("=")[1]
            .split(",")[1]
            .toLong()

        LogUtils.d("PlotterPrint", "legacy handshake value=$handshakeValue")

        device.send(PrintUtil.getHandshake(handshakeValue), object : DeviceManager.Callback {
            override fun data(success: Boolean, received: Received?) {
                val handshakeReadData = received?.readData
                LogUtils.d(
                    "PlotterPrint",
                    "legacy handshake callback success=$success received=${handshakeReadData?.take(120)}"
                )
                if (handshakeReadData.isNullOrEmpty()) {
                    onResult(PrintResult(false, "Handshake failed"))
                    return
                }

                handleLegacyHandshakeResponse(device, plt, handshakeReadData, onResult)
            }
        })
    } catch (e: Exception) {
        LogUtils.e("PlotterPrint", "legacy state handling failed: ${e.message}")
        onResult(PrintResult(false, "Error processing state response"))
    }
}

private fun handleLegacyHandshakeResponse(
    device: DeviceManager,
    plt: String,
    readData: String,
    onResult: (PrintResult) -> Unit
) {
    try {
        val response = StringUtil.hexStringToString(readData)
        LogUtils.d("PlotterPrint", "legacy handshake response=$response")
        if (response.contains("RCMD=12")) {
            device.t485.isPrint = true
            device.sendNoBack(PrintUtil.printFile(plt), object : DeviceManager.Callback {
                override fun data(success: Boolean, received: Received?) {
                    LogUtils.d(
                        "PlotterPrint",
                        "legacy sendNoBack callback success=$success received=${received?.readData?.take(120)}"
                    )
                    onResult(
                        if (success) {
                            PrintResult(true, "File sent successfully")
                        } else {
                            PrintResult(false, "Failed to send file")
                        }
                    )
                }
            })
        } else {
            LogUtils.e("PlotterPrint", "legacy handshake rejected: $response")
            onResult(PrintResult(false, "Unexpected handshake response"))
        }
    } catch (e: Exception) {
        LogUtils.e("PlotterPrint", "legacy handshake handling failed: ${e.message}")
        onResult(PrintResult(false, "Error processing handshake response"))
    }
}

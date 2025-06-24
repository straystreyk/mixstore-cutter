package com.pokrikinc.mixpokrikcutter.plotter


fun DeviceManager.printFile(plt: String) {
    require(plt.isNotEmpty()) { "PLT file path cannot be empty" }

    send(PrintUtil.getState(), object : DeviceManager.Callback {
        override fun data(success: Boolean, received: Received?) {
            if (!success || received == null) {
                LogUtils.d("[Debug]", "Failed to get device state")
                return
            }

            handleStateResponse(this@printFile, plt, received)
        }
    })
}

private fun handleStateResponse(device: DeviceManager, plt: String, received: Received) {
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
                    return
                }
                handleHandshakeResponse(device, plt, received)
            }
        })

    } catch (e: Exception) {
        LogUtils.d("handleStateResponse", "Error processing state response")
    }
}

private fun handleHandshakeResponse(device: DeviceManager, plt: String, received: Received) {
    try {
        val readData = received.readData ?: ""
        val response = StringUtil.hexStringToString(readData)
        LogUtils.d("handleHandshakeResponse", "Handshake response: $response")

        if (response.contains("RCMD=12")) {
            device.sendNoBack(PrintUtil.printFile(plt), object : DeviceManager.Callback {
                override fun data(success: Boolean, received: Received?) {
                    if (success) {
                        LogUtils.d("handleHandshakeResponse", "File sent successfully")
                    } else {
                        LogUtils.d("handleHandshakeResponse", "Failed to send file")
                    }
                }
            })
        } else {
            LogUtils.d("handleHandshakeResponse", "File sent successfully")
            LogUtils.e("handleHandshakeResponse", "File sent successfully")
        }
    } catch (e: Exception) {
        LogUtils.e("handleHandshakeResponse", "Error processing handshake response")
    }
}
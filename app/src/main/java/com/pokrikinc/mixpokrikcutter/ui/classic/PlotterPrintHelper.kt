package com.pokrikinc.mixpokrikcutter.ui.classic

import android.content.Context
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.data.repository.CatalogRepository
import com.pokrikinc.mixpokrikcutter.plotter.DeviceManager
import com.pokrikinc.mixpokrikcutter.plotter.PrintResult
import com.pokrikinc.mixpokrikcutter.plotter.PrintUtil
import com.pokrikinc.mixpokrikcutter.plotter.Received
import com.pokrikinc.mixpokrikcutter.plotter.printFileAwait
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlotterPrintHelper {
    suspend fun printPartByAttFile(context: Context, attFile: String): PrintResult =
        withContext(Dispatchers.IO) {
            val plts = AppDataStore.ensurePltsLoaded(context)
            val pltName = plts[attFile]?.asString
                ?: return@withContext PrintResult(false, "Print file is unavailable")

            val pltContent = CatalogRepository.loadFile(context, "files/$pltName")
            if (pltContent.isBlank()) {
                return@withContext PrintResult(false, "Print file is unavailable")
            }

            printRawPltContent(pltContent)
        }

    suspend fun printRawPltContent(pltContent: String): PrintResult =
        withContext(Dispatchers.IO) {
            if (pltContent.isBlank()) {
                return@withContext PrintResult(false, "Print file is unavailable")
            }

            val speed = PreferenceManager.getPrintSpeed()
            val pressure = PreferenceManager.getPrintPressure()
            val deviceManager = AppDataStore.ensureDeviceManager()
                ?: return@withContext PrintResult(false, "Device is unavailable")

            applyPlotterSettings(deviceManager, speed, pressure)
            var result = deviceManager.printFileAwait(pltContent)
            if (!result.isSuccess) {
                val reconnectedManager = AppDataStore.reconnectDeviceManager()
                if (reconnectedManager != null) {
                    applyPlotterSettings(reconnectedManager, speed, pressure)
                    result = reconnectedManager.printFileAwait(pltContent)
                }
            }
            result
        }

    suspend fun applyPlotterSettings(
        deviceManager: DeviceManager,
        speed: Int,
        pressure: Int
    ) {
        sendSetting(deviceManager, PrintUtil.setSpeedV(speed.coerceIn(1, 4)))
        sendSetting(deviceManager, PrintUtil.setSpeedF(pressure.coerceAtLeast(1)))
    }

    private fun sendSetting(deviceManager: DeviceManager, command: ByteArray) {
        try {
            deviceManager.send(command, object : DeviceManager.Callback {
                override fun data(success: Boolean, received: Received?) {
                }
            })
        } catch (_: Exception) {
        }
    }
}

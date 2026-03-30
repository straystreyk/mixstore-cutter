package com.pokrikinc.mixpokrikcutter.ui.classic

import android.content.Context
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.data.repository.CatalogRepository
import com.pokrikinc.mixpokrikcutter.plotter.DeviceManager
import com.pokrikinc.mixpokrikcutter.plotter.LogUtils
import com.pokrikinc.mixpokrikcutter.plotter.PrintResult
import com.pokrikinc.mixpokrikcutter.plotter.printFileAwait
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object PlotterPrintHelper {
    suspend fun printPartByAttFile(context: Context, attFile: String): PrintResult =
        withContext(Dispatchers.IO) {
            LogUtils.d("PlotterPrint", "printPartByAttFile attFile=$attFile")
            val plts = AppDataStore.ensurePltsLoaded(context)
            val pltName = plts[attFile]?.asString
                ?: return@withContext PrintResult(false, "Print file is unavailable")
                    .also { LogUtils.e("PlotterPrint", "PLT mapping missing for attFile=$attFile") }

            val pltContent = CatalogRepository.loadFile(context, "files/$pltName")
            if (pltContent.isBlank()) {
                LogUtils.e("PlotterPrint", "PLT content is blank for file=$pltName")
                return@withContext PrintResult(false, "Print file is unavailable")
            }

            printRawPltContent(pltContent)
        }

    suspend fun printRawPltContent(pltContent: String): PrintResult =
        withContext(Dispatchers.IO) {
            if (pltContent.isBlank()) {
                LogUtils.e("PlotterPrint", "printRawPltContent aborted: blank content")
                return@withContext PrintResult(false, "Print file is unavailable")
            }

            LogUtils.d("PlotterPrint", "printRawPltContent pltLength=${pltContent.length}")
            val deviceManager = AppDataStore.ensureDeviceManager()
                ?: return@withContext PrintResult(false, "Device is unavailable")
                    .also { LogUtils.e("PlotterPrint", "DeviceManager unavailable before print") }

            var result = deviceManager.printFileAwait(pltContent)
            LogUtils.d("PlotterPrint", "Primary print result success=${result.isSuccess} message=${result.message}")
            if (!result.isSuccess) {
                LogUtils.d("PlotterPrint", "Attempting reconnect after failed print")
                val reconnectedManager = AppDataStore.reconnectDeviceManager()
                if (reconnectedManager != null) {
                    result = reconnectedManager.printFileAwait(pltContent)
                    LogUtils.d("PlotterPrint", "Reconnect print result success=${result.isSuccess} message=${result.message}")
                } else {
                    LogUtils.e("PlotterPrint", "Reconnect failed: device manager is null")
                }
            }
            result
        }
}

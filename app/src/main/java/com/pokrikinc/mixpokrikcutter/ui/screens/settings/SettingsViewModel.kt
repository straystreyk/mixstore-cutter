package com.pokrikinc.mixpokrikcutter.ui.screens.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager

/**
 * Проверяет, является ли строка валидным URL:
 * - Должна быть схема (http/https) или IP:PORT (но порт в пределах 0-65535)
 * - Не должно быть лишних символов
 */
private fun isUrlValid(url: String): Boolean {
    return try {
        // Если нет http/https, добавляем временно для проверки
        val checkUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "http://$url"
        } else {
            url
        }

        val uri = java.net.URI(checkUrl)

        // Проверяем, что host не null (значит, строка похожа на URL)
        uri.host != null &&
                // Если есть порт, проверяем его валидность (0-65535)
                (uri.port == -1 || uri.port in 0..65535) &&
                // Дополнительно проверяем, нет ли кривых символов
                url.matches(Regex("^[a-zA-Z0-9.:/_-]+$"))
    } catch (e: Exception) {
        false
    }
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    var urlText by mutableStateOf("")
    var printerName by mutableStateOf("")
    var printSpeed by mutableIntStateOf(4)

    init {
        urlText = PreferenceManager.getBaseUrl()
        printerName = PreferenceManager.getPrinterName()
        printSpeed = PreferenceManager.getPrintSpeed()
    }

    fun onUrlChange(new: String) {
        urlText = new
    }

    fun onPrinterNameChange(new: String) {
        printerName = new
    }

    fun onPrintSpeedChange(new: Int) {
        printSpeed = new
    }

    fun saveAllSettings() {
        val validatedUrl = if (urlText.isNotBlank() && isUrlValid(urlText)) {
            urlText
        } else {
            "http://192.168.1.54:8080"
        }

        urlText = validatedUrl

        PreferenceManager.setBaseUrl(validatedUrl)
        PreferenceManager.setPrinterName(printerName)
        PreferenceManager.setPrintSpeed(printSpeed)

        RetrofitProvider.updateBaseUrl(validatedUrl)
    }
}



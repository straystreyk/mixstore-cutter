package com.pokrikinc.mixpokrikcutter.ui.screens.settings

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager


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
        PreferenceManager.setBaseUrl(urlText)
        PreferenceManager.setPrinterName(printerName)
        PreferenceManager.setPrintSpeed(printSpeed)

        RetrofitProvider.updateBaseUrl(urlText)
    }
}



package com.pokrikinc.mixpokrikcutter.store

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object PreferenceManager {
    private const val PREF_NAME = "app_prefs"
    private const val KEY_BASE_URL = "base_url"
    private const val KEY_PRINTER_NAME = "printer_name"
    private const val KEY_PRINT_SPEED = "print_speed"
    private const val KEY_PRINT_PRESSURE = "print_pressure"
    private const val DEFAULT_BASE_URL = "http://192.168.50.43:8080"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getBaseUrl() =
        prefs.getString(KEY_BASE_URL, DEFAULT_BASE_URL) ?: DEFAULT_BASE_URL

    fun setBaseUrl(url: String) = prefs.edit { putString(KEY_BASE_URL, url) }

    fun getPrinterName() = prefs.getString(KEY_PRINTER_NAME, "") ?: ""
    fun setPrinterName(name: String) = prefs.edit { putString(KEY_PRINTER_NAME, name) }

    fun getPrintSpeed() = prefs.getInt(KEY_PRINT_SPEED, 4)
    fun setPrintSpeed(speed: Int) = prefs.edit { putInt(KEY_PRINT_SPEED, speed) }

    fun getPrintPressure() = prefs.getInt(KEY_PRINT_PRESSURE, 4)
    fun setPrintPressure(pressure: Int) = prefs.edit { putInt(KEY_PRINT_PRESSURE, pressure) }
}

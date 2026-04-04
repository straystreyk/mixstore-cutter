package com.pokrikinc.mixpokrikcutter.ui.classic

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import com.pokrikinc.mixpokrikcutter.AppDataStore
import com.pokrikinc.mixpokrikcutter.BuildConfig
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.plotter.DeviceManager
import com.pokrikinc.mixpokrikcutter.plotter.PrintUtil
import com.pokrikinc.mixpokrikcutter.plotter.Received
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (requireActivity() as MainActivity).setTitleAndBack(
            getString(R.string.title_settings),
            false
        )

        val versionText = view.findViewById<TextView>(R.id.text_app_version)
        val urlInput = view.findViewById<EditText>(R.id.input_base_url)
        val printerInput = view.findViewById<EditText>(R.id.input_printer_name)
        val speedInput = view.findViewById<EditText>(R.id.input_print_speed)
        val pressureInput = view.findViewById<EditText>(R.id.input_print_pressure)
        val saveButton = view.findViewById<Button>(R.id.button_save)
        val deviceSettingsButton = view.findViewById<Button>(R.id.button_device_settings)
        val checkUpdatesButton = view.findViewById<Button>(R.id.button_check_updates)

        versionText.text = getString(
            R.string.app_version_value,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE
        )
        urlInput.setText(PreferenceManager.getBaseUrl())
        printerInput.setText(PreferenceManager.getPrinterName())
        speedInput.setText(PreferenceManager.getPrintSpeed().toString())
        pressureInput.setText(PreferenceManager.getPrintPressure().toString())

        saveButton.setOnClickListener {
            val url = urlInput.text?.toString().orEmpty().ifBlank { PreferenceManager.getBaseUrl() }
            val printerName = printerInput.text?.toString().orEmpty()
            val speed = speedInput.text?.toString()?.toIntOrNull()?.coerceIn(1, 4) ?: 4
            val pressure = pressureInput.text?.toString()?.toIntOrNull()
                ?.coerceAtLeast(1)
                ?: PreferenceManager.getPrintPressure()

            PreferenceManager.setBaseUrl(url)
            PreferenceManager.setPrinterName(printerName)
            PreferenceManager.setPrintSpeed(speed)
            PreferenceManager.setPrintPressure(pressure)
            RetrofitProvider.updateBaseUrl(url)
            applyPlotterSettings(speed, pressure)

            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show()
        }

        deviceSettingsButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_SETTINGS))
        }

        checkUpdatesButton.setOnClickListener {
            (requireActivity() as MainActivity).checkForAppUpdates(silent = false)
        }
    }

    private fun applyPlotterSettings(speed: Int, pressure: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            val deviceManager = AppDataStore.ensureDeviceManager() ?: return@launch
            sendSetting(deviceManager, PrintUtil.setSpeedV(speed.coerceIn(1, 4)))
            sendSetting(deviceManager, PrintUtil.setSpeedF(pressure.coerceAtLeast(1)))
        }
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

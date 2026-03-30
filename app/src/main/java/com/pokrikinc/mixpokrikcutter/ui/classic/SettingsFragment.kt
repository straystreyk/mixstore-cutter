package com.pokrikinc.mixpokrikcutter.ui.classic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.pokrikinc.mixpokrikcutter.MainActivity
import com.pokrikinc.mixpokrikcutter.R
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.store.PreferenceManager

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

        val urlInput = view.findViewById<EditText>(R.id.input_base_url)
        val printerInput = view.findViewById<EditText>(R.id.input_printer_name)
        val speedInput = view.findViewById<EditText>(R.id.input_print_speed)
        val saveButton = view.findViewById<Button>(R.id.button_save)

        urlInput.setText(PreferenceManager.getBaseUrl())
        printerInput.setText(PreferenceManager.getPrinterName())
        speedInput.setText(PreferenceManager.getPrintSpeed().toString())

        saveButton.setOnClickListener {
            val url = urlInput.text?.toString().orEmpty().ifBlank { PreferenceManager.getBaseUrl() }
            val printerName = printerInput.text?.toString().orEmpty()
            val speed = speedInput.text?.toString()?.toIntOrNull()?.coerceIn(1, 4) ?: 4

            PreferenceManager.setBaseUrl(url)
            PreferenceManager.setPrinterName(printerName)
            PreferenceManager.setPrintSpeed(speed)
            RetrofitProvider.updateBaseUrl(url)

            Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show()
        }
    }
}

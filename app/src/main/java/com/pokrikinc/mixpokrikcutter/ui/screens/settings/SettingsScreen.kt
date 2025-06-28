package net.ezcoder.ezprinter.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokrikinc.mixpokrikcutter.ui.screens.settings.SettingsViewModel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val url by remember { derivedStateOf { viewModel.urlText } }
    val printerName by remember { derivedStateOf { viewModel.printerName } }
    val printSpeed by remember { derivedStateOf { viewModel.printSpeed.toString() } }
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Настройки", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = url,
            onValueChange = viewModel::onUrlChange,
            label = { Text("URL очередей") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = printerName,
            onValueChange = viewModel::onPrinterNameChange,
            label = { Text("Имя принтера") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = printSpeed,
            onValueChange = { input ->
                // Фильтруем только цифры
                val filtered = input.filter { it.isDigit() }

                // Ограничиваем длину (1 символ, т.к. максимум 4)
                val limited = if (filtered.length > 1) filtered.take(1) else filtered

                // Парсим число и проверяем границы 1..4
                val speedInt = limited.toIntOrNull()?.coerceIn(1, 4) ?: 1

                viewModel.onPrintSpeedChange(speedInt)
            },
            label = { Text("Скорость реза (1-4)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true
        )

        Button(
            onClick = {
                viewModel.saveAllSettings()
                Toast.makeText(ctx, "Настройки успешно сохранены", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Сохранить")
        }
    }
}


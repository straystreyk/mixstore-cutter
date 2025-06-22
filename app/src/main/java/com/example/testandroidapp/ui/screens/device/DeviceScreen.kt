package com.example.testandroidapp.ui.screens.device

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.testandroidapp.data.model.Device
import com.example.testandroidapp.ui.navigation.LocalNavController
import com.example.testandroidapp.ui.screens.LocalCatalogData

@Composable
fun DeviceScreen(
    categoryId: String?,
    vendorId: String?
) {
    val navController = LocalNavController.current
    val catalogData = LocalCatalogData.current

    // Мемоизируем вычисления для избежания пересчета при рекомпозиции
    val devices = remember(categoryId, vendorId, catalogData) {
        catalogData
            .find { it.id == categoryId }
            ?.vendors
            ?.find { it.id == vendorId }
            ?.devices
    }

    when {
        categoryId.isNullOrBlank() || vendorId.isNullOrBlank() -> {
            ErrorMessage("Не переданы обязательные параметры")
        }

        devices.isNullOrEmpty() -> {
            ErrorMessage("Устройства не найдены")
        }

        else -> {
            DeviceGrid(
                devices = devices,
                onDeviceClick = { device ->
                    navController.navigate("catalog/$categoryId/$vendorId/${device.id}")
                }
            )
        }
    }
}

@Composable
private fun DeviceGrid(
    devices: List<Device>, // Предполагается, что Device - это ваша модель данных
    onDeviceClick: (Device) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        columns = GridCells.Adaptive(minSize = 160.dp), // Адаптивные колонки
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = devices,
            key = { device -> device.id }
        ) { device ->
            DeviceCard(
                device = device,
                onClick = { onDeviceClick(device) }
            )
        }
    }
}

@Composable
private fun DeviceCard(
    device: Device, // Замените на вашу модель данных
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
package com.example.testandroidapp.ui.screens.device


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testandroidapp.ui.screens.LocalCatalogData

@Composable
fun DeviceScreen(
    categoryId: String?,
    vendorId: String?
) {
    val category = LocalCatalogData.current.find { it -> it.id == categoryId }
    val vendors = category?.vendors
    val currentVendor = vendors?.find { it -> it.id == vendorId }
    val devices = currentVendor?.devices

    if (categoryId !== null && vendorId !== null && devices !== null) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            columns = GridCells.Fixed(2), // 2 колонки (можно адаптивно настроить)
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices.size, key = { index -> devices[index].id }) { index ->
                val device = devices[index]

                Card(
                    modifier = Modifier
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Text(device.name, style = MaterialTheme.typography.titleLarge)

                    }
                }
            }
        }
    } else {
        Text("Не передан вендор или категория")
    }
}






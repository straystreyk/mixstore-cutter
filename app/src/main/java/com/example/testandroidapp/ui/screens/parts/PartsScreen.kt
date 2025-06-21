package com.example.testandroidapp.ui.screens.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.example.testandroidapp.plotter.DeviceManager
import com.example.testandroidapp.plotter.PrintUtil
import com.example.testandroidapp.plotter.Received
import com.example.testandroidapp.ui.screens.LocalCatalogData
import com.example.testandroidapp.ui.screens.LocalImagesData

@Composable
fun PartsScreen(
    categoryId: String?,
    vendorId: String?,
    deviceId: String?
) {
    val devicemanager = DeviceManager.getInstance()
    val context = LocalContext.current
    val images = LocalImagesData.current
    val category = LocalCatalogData.current.find { it -> it.id == categoryId }
    val vendors = category?.vendors
    val currentVendor = vendors?.find { it -> it.id == vendorId }
    val devices = currentVendor?.devices
    val parts = devices?.find { it -> it.id == deviceId }
    val partList = parts?.partlist


    if (partList?.isNotEmpty() == true) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            columns = GridCells.Fixed(1), // 2 колонки (можно адаптивно настроить)
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(partList.size, key = { index -> partList[index].attfile }) { index ->
                val part = partList[index]
                val img = "file:///android_asset/files/${images[part.picfile].asString}"

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.background,
                    )
                ) {
                    Row {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(img)
                                .crossfade(true)
                                .decoderFactory(SvgDecoder.Factory())
                                .build(),
                            contentDescription = "Изображение категории",
                            modifier = Modifier
                                .width(150.dp)
                        )
                        Column {
                            Text(part.name)
                            Button(
                                onClick = {
                                    devicemanager.start485()
                                    devicemanager.send(
                                        PrintUtil.square(),
                                        object : DeviceManager.Callback {
                                            override fun data(
                                                success: Boolean,
                                                received: Received?
                                            ) {
                                                if (success) {
                                                    val responseData = received?.readData
                                                    println(responseData)
                                                    println("Кайф!!!")
                                                } else {
                                                    println("Ошибка")
                                                }
                                            }
                                        })
                                },
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text("Печать")
                            }
                        }
                    }
                }
            }
        }
    } else {
        Text("Не передан вендор или категория")
    }
}






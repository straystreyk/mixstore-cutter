package com.pokrikinc.mixpokrikcutter.ui.screens.parts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pokrikinc.mixpokrikcutter.data.model.Part
import com.pokrikinc.mixpokrikcutter.data.repository.CatalogRepository
import com.pokrikinc.mixpokrikcutter.plotter.DeviceManager
import com.pokrikinc.mixpokrikcutter.plotter.printFile
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalCatalogData
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalDeviceInstance
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalImagesData

@Composable
fun PartsScreen(
    categoryId: String?,
    vendorId: String?,
    deviceId: String?
) {
    var plts by remember { mutableStateOf<JsonObject?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val images = LocalImagesData.current
    val deviceInstance = LocalDeviceInstance.current
    val catalogData = LocalCatalogData.current

    // Мемоизация вычисления деталей
    val partList = remember(categoryId, vendorId, deviceId, catalogData) {
        catalogData
            .find { it.id == categoryId }
            ?.vendors
            ?.find { it.id == vendorId }
            ?.devices
            ?.find { it.id == deviceId }
            ?.partlist
    }

    // Загрузка данных plts.json
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            error = null
            val pltsString = CatalogRepository.loadFile(context, "plts.json")
            plts = Gson().fromJson(pltsString, JsonObject::class.java)
        } catch (e: Exception) {
            error = "Ошибка загрузки данных: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    when {
        isLoading -> LoadingState()
        error != null -> ErrorState(error ?: "")
        categoryId.isNullOrBlank() || vendorId.isNullOrBlank() || deviceId.isNullOrBlank() -> {
            ErrorState("Не переданы обязательные параметры")
        }

        partList.isNullOrEmpty() -> EmptyState()
        plts == null -> ErrorState("Не удалось загрузить данные plts")
        else -> PartsList(
            parts = partList,
            plts = plts ?: JsonObject(),
            images = images,
            deviceInstance = deviceInstance,
            context = context
        )
    }
}

@Composable
private fun PartsList(
    parts: List<Part>,
    plts: JsonObject,
    images: JsonObject,
    deviceInstance: DeviceManager?, // Замените на точный тип
    context: android.content.Context
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = parts,
            key = { part -> part.attfile }
        ) { part ->
            PartCard(
                part = part,
                plts = plts,
                images = images,
                deviceInstance = deviceInstance,
                context = context
            )
        }
    }
}

@Composable
private fun PartCard(
    part: Part,
    plts: JsonObject,
    images: JsonObject,
    deviceInstance: DeviceManager?, // Замените на точный тип
    context: android.content.Context
) {
    // Мемоизация URL изображения
    val imageUrl = remember(part.picfile, images) {
        "file:///android_asset/files/${images[part.picfile]?.asString}"
    }

    // Мемоизация функции печати
    val handlePrint = remember(part, plts, deviceInstance) {
        {
            try {
                val pltPath = CatalogRepository.loadFile(
                    context,
                    "files/${plts[part.attfile]?.asString}"
                )
                deviceInstance?.printFile(pltPath)
            } catch (e: Exception) {
                // Здесь можно добавить обработку ошибок, например показ Toast
                // Log.e("PartsScreen", "Ошибка печати", e)
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Изображение детали
            PartImage(
                imageUrl = imageUrl,
                contentDescription = part.name,
                context = context
            )

            // Информация о детали
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            ) {
                Text(
                    text = part.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { handlePrint() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Печать")
                }
            }
        }
    }
}

@Composable
private fun PartImage(
    imageUrl: String,
    contentDescription: String,
    context: android.content.Context
) {
    AsyncImage(
        model = remember(imageUrl) {
            ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(true)
                .decoderFactory(SvgDecoder.Factory())
                .build()
        },
        contentDescription = contentDescription,
        modifier = Modifier.size(120.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Загрузка...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun ErrorState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Детали не найдены",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}
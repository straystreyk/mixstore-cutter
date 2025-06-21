package com.example.testandroidapp.ui.screens.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.testandroidapp.data.repository.CatalogRepository
import com.example.testandroidapp.ui.components.CategoryCard
import com.example.testandroidapp.ui.navigation.LocalNavController
import com.example.testandroidapp.ui.screens.LocalCatalogData
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VendorScreen(
    categoryId: String?
) {
    var images by remember { mutableStateOf<JsonObject?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val context = LocalContext.current
    val navController = LocalNavController.current

    // Получаем данные
    val catalogData = LocalCatalogData.current
    val category = catalogData.find { it.id == categoryId }
    val vendors = category?.vendors ?: emptyList()

    val vendorItems by remember {
        derivedStateOf {
            vendors.map { vendor ->
                val imageName = images?.get(vendor.img)
                val imgUrl = if (imageName != null) {
                    "file:///android_asset/files/${imageName.asString}"
                } else {
                    ""
                }
                VendorItem(
                    id = vendor.id,
                    name = vendor.name,
                    imgUrl = imgUrl
                )
            }
        }
    }

    LaunchedEffect(categoryId) {
        withContext(Dispatchers.IO) {
            val imagesString = CatalogRepository.loadFile(context, "images.json")
            images = Gson().fromJson(imagesString, JsonObject::class.java)
        }
        isLoading = false
    }

    when {
        categoryId == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Не передана категория")
            }
        }

        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        vendors.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Список поставщиков пуст")
            }
        }

        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = vendorItems,
                    key = { it.id } // Важно для оптимизации
                ) { vendorItem ->
                    CategoryCard(
                        modifier = Modifier.fillMaxWidth(),
                        title = vendorItem.name,
                        img = vendorItem.imgUrl,
                        onClick = {
                            navController.navigate("catalog/${categoryId}/${vendorItem.id}")
                        }
                    )
                }
            }
        }
    }
}

// Данные для элемента списка
private data class VendorItem(
    val id: String,
    val name: String,
    val imgUrl: String
)
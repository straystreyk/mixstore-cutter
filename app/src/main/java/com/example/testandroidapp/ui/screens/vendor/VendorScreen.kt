package com.example.testandroidapp.ui.screens.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testandroidapp.ui.components.CategoryCard
import com.example.testandroidapp.ui.navigation.LocalNavController
import com.example.testandroidapp.ui.screens.LocalCatalogData
import com.example.testandroidapp.ui.screens.LocalImagesData

@Composable
fun VendorScreen(
    categoryId: String?
) {
    val images = LocalImagesData.current
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

    when {
        categoryId == null -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Не передана категория")
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
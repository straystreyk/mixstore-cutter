package com.pokrikinc.mixpokrikcutter.ui.screens.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pokrikinc.mixpokrikcutter.ui.components.CategoryCard
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalNavController
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalCatalogData
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalImagesData

@Composable
fun VendorScreen(
    categoryId: String?
) {
    val images = LocalImagesData.current
    val navController = LocalNavController.current
    val lazyListState = rememberLazyListState()

    // Получаем данные
    val catalogData = LocalCatalogData.current
    val category = remember(catalogData, categoryId) {
        catalogData.find { it.id == categoryId }
    }
    val vendors = remember(category) { category?.vendors ?: emptyList() }

    // Мемоизируем список элементов
    val vendorItems by remember(vendors, images) {
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

    // Состояние пагинации
    var visibleItems by remember { mutableStateOf(40) }
    println(visibleItems)

    // Подгружаем следующие элементы при прокрутке
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.layoutInfo.visibleItemsInfo }
            .collect { visibleItemsInfo ->
                if (visibleItemsInfo.isNotEmpty() &&
                    visibleItemsInfo.last().index >= visibleItems - 5 &&
                    visibleItems < vendorItems.size
                ) {
                    visibleItems = (visibleItems + 15).coerceAtMost(vendorItems.size)
                }
            }
    }

    // Мемоизируем обработчик клика
    val handleVendorClick = remember(navController, categoryId) {
        { vendorId: String ->
            navController.navigate("catalog/${categoryId}/${vendorId}")
        }
    }

    when {
        categoryId == null -> {
            ErrorMessage("Не передана категория")
        }

        vendorItems.isEmpty() -> {
            ErrorMessage("Список поставщиков пуст")
        }

        else -> {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(
                    items = vendorItems.take(visibleItems),
                    key = { _, item -> item.id }
                ) { _, vendorItem ->
                    VendorCard(
                        vendorItem = vendorItem,
                        onClick = { handleVendorClick(vendorItem.id) }
                    )
                }

                // Показываем индикатор загрузки, если есть еще элементы
                if (visibleItems < vendorItems.size) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VendorCard(
    vendorItem: VendorItem,
    onClick: () -> Unit
) {
    CategoryCard(
        modifier = Modifier.fillMaxWidth(),
        title = vendorItem.name,
        img = vendorItem.imgUrl,
        onClick = onClick
    )
}

@Composable
private fun ErrorMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(message)
    }
}

private data class VendorItem(
    val id: String,
    val name: String,
    val imgUrl: String
)
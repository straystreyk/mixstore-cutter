package com.pokrikinc.mixpokrikcutter.ui.screens.vendor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokrikinc.mixpokrikcutter.ui.components.CategoryCard
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalNavController
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalTitleViewModel
import com.pokrikinc.mixpokrikcutter.ui.navigation.defaultTitle
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalCatalogData
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalImagesData

@Composable
fun VendorScreen(
    categoryId: String?,
    viewModel: VendorViewModel = viewModel()
) {
    val images = LocalImagesData.current
    val catalogData = LocalCatalogData.current
    val navController = LocalNavController.current
    val titleViewModel = LocalTitleViewModel.current
    val lazyListState = rememberLazyListState()
    val vendorItems by viewModel.vendorItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val category = catalogData.find { it.id == categoryId }

    // Загружаем данные один раз
    LaunchedEffect(categoryId, catalogData, images) {
        titleViewModel.setTitle(category?.name ?: defaultTitle)
        viewModel.loadVendors(category, images)
    }

    if (errorMessage != null) {
        ErrorMessage(errorMessage!!)
        return
    }

    LazyColumn(
        state = lazyListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = vendorItems,
            key = { it.id },
            contentType = { "vendorItem" }
        ) { vendorItem ->
            VendorCard(
                vendorItem = vendorItem,
                onClick = {
                    navController.navigate("catalog/${categoryId}/${vendorItem.id}")
                }
            )
        }
    }
}

@Composable
private fun VendorCard(
    vendorItem: VendorItem,
    onClick: () -> Unit
) {
    println("VendorCard recomposed: ${vendorItem.name}")

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

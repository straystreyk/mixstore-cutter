package com.pokrikinc.mixpokrikcutter.ui.screens.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pokrikinc.mixpokrikcutter.ui.components.CategoryCard
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalNavController
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalTitleViewModel
import com.pokrikinc.mixpokrikcutter.ui.navigation.defaultTitle
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalCatalogData
import com.pokrikinc.mixpokrikcutter.ui.screens.LocalImagesData

@Composable
fun MainScreen() {
    val images = LocalImagesData.current
    val catalog = LocalCatalogData.current
    val titleViewModel = LocalTitleViewModel.current
    val navController = LocalNavController.current

    LaunchedEffect(Unit) {
        titleViewModel.setTitle(defaultTitle)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        items(catalog) { item ->
            val imageName = images[item.img]
            val imgUrl = imageName?.asString?.let {
                "file:///android_asset/files/$it"
            } ?: ""

            CategoryCard(
                modifier = Modifier
                    .fillMaxSize(),
                title = item.name,
                img = imgUrl,
                onClick = {
                    navController.navigate("catalog/${item.id}")
                }
            )
        }
    }
}




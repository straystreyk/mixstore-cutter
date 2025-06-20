package com.example.testandroidapp.ui.screens.category


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testandroidapp.ui.components.CategoryCard
import com.example.testandroidapp.ui.navigation.LocalNavController
import com.example.testandroidapp.ui.screens.LocalCatalogData

@Composable
fun CategoryScreen(
    categoryId: String?
) {
    val category = LocalCatalogData.current.find { it -> it.id == categoryId }
    val vendors = category?.vendors
    val navController = LocalNavController.current


    if (categoryId !== null && vendors?.isNotEmpty() == true) {
        LazyVerticalGrid(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            columns = GridCells.Fixed(2), // 2 колонки (можно адаптивно настроить)
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(vendors.size, key = { index -> vendors[index].id }) { index ->
                val vendor = vendors[index]
                CategoryCard(
                    title = vendor.name,
                    img = vendor.img,
                    onClick = {
                        navController.navigate("catalog/${categoryId}/${vendor.id}")
                    }
                )
            }
        }
    } else {
        Text("Не передана категория")
    }


}

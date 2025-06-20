package com.example.testandroidapp.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.testandroidapp.ui.components.CategoryCard
import com.example.testandroidapp.ui.navigation.LocalNavController
import com.example.testandroidapp.ui.screens.LocalCatalogData

@Composable
fun MainScreen() {
    val catalog = LocalCatalogData.current
    val navController = LocalNavController.current

    // Разбиваем список на пары для 2 столбцов
    val rows = catalog.chunked(2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Первый элемент в строке
                Box(modifier = Modifier.weight(1f)) {
                    CategoryCard(
                        title = rowItems[0].name,
                        img = rowItems[0].img,
                        onClick = {
                            println("asdasdsa")
                            navController.navigate("catalog/${rowItems[0].id}")
                        }
                    )
                }

                // Второй элемент или пустое место
                if (rowItems.size > 1) {
                    Box(modifier = Modifier.weight(1f)) {
                        CategoryCard(
                            title = rowItems[1].name,
                            img = rowItems[1].img,
                            onClick = {
                                println("asdasdsa")
                                navController.navigate("catalog/${rowItems[1].id}")
                            }
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }

            // Отступ между строками
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}





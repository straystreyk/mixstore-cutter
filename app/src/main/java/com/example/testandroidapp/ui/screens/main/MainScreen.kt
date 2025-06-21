package com.example.testandroidapp.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.testandroidapp.data.repository.CatalogRepository
import com.example.testandroidapp.ui.components.CategoryCard
import com.example.testandroidapp.ui.navigation.LocalNavController
import com.example.testandroidapp.ui.screens.LocalCatalogData
import com.google.gson.Gson
import com.google.gson.JsonObject

@Composable
fun MainScreen() {
    val images = remember { mutableStateOf(JsonObject()) }
    val context = LocalContext.current
    val catalog = LocalCatalogData.current
    val navController = LocalNavController.current

    // Разбиваем список на пары для 2 столбцов
    val rows = catalog.chunked(2)

    LaunchedEffect(Unit) {
        val imagesString = CatalogRepository.loadFile(context, "images.json")
        images.value = Gson().fromJson(imagesString, JsonObject::class.java)
    }



    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                println()


                // Первый элемент в строке
                Box(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    val imageName = images.value.get(rowItems[0].img)
                    val imgUrl =
                        if (imageName !== null) "file:///android_asset/files/${imageName.asString}" else ""

                    CategoryCard(
                        modifier = Modifier.fillMaxHeight(),
                        title = rowItems[0].name,
                        img = imgUrl,
                        onClick = {
                            navController.navigate("catalog/${rowItems[0].id}")
                        }
                    )
                }

                // Второй элемент или пустое место
                if (rowItems.size > 1) {
                    val imageName = images.value.get(rowItems[1].img)
                    val imgUrl =
                        if (imageName !== null) "file:///android_asset/files/${imageName.asString}" else ""

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        CategoryCard(
                            modifier = Modifier.fillMaxHeight(),
                            title = rowItems[1].name,
                            img = imgUrl,
                            onClick = {
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





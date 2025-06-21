package com.example.testandroidapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.testandroidapp.data.model.Category
import com.example.testandroidapp.data.repository.CatalogRepository
import com.example.testandroidapp.ui.navigation.AppNavigation
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalCatalogData = compositionLocalOf<List<Category>> { emptyList() }
val LocalImagesData = compositionLocalOf { JsonObject() }

@Composable
fun PokrikApp() {
    val context = LocalContext.current // Получаем контекст здесь
    var isLoading by remember { mutableStateOf(true) }
    var catalogData by remember { mutableStateOf<List<Category>>(emptyList()) }
    var images by remember { mutableStateOf(JsonObject()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val data = CatalogRepository.loadCatalogFromAssets(context, "db.json")
            val imagesString = CatalogRepository.loadFile(context, "images.json")
            images = Gson().fromJson(imagesString, JsonObject::class.java)
            catalogData = data
            isLoading = false
        }
    }


    CompositionLocalProvider(
        LocalCatalogData provides catalogData,
        LocalImagesData provides images
    ) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            catalogData.isNotEmpty() -> {
                AppNavigation()
            }
        }

    }
}
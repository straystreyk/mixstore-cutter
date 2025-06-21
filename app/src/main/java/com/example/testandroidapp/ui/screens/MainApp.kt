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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

val LocalCatalogData = compositionLocalOf<List<Category>> { emptyList() }


@Composable
fun PokrikApp() {
    val context = LocalContext.current // Получаем контекст здесь
    var isLoading by remember { mutableStateOf(true) }
    var catalogData by remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val data = CatalogRepository.loadCatalogFromAssets(context, "db.json")
            catalogData = data
            isLoading = false
        }
    }


    CompositionLocalProvider(
        LocalCatalogData provides catalogData
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
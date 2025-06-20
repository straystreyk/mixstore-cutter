package com.example.testandroidapp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.testandroidapp.data.model.Category
import com.example.testandroidapp.data.repository.CatalogRepository
import com.example.testandroidapp.ui.navigation.AppNavigation

val LocalCatalogData = compositionLocalOf<List<Category>> { emptyList() }


@Composable
fun PokrikApp() {
    val context = LocalContext.current // Получаем контекст здесь
    val isLoading = remember { mutableStateOf(true) }
    val catalogData = remember { mutableStateOf<List<Category>>(emptyList()) }

    LaunchedEffect(Unit) {
        val data = CatalogRepository.loadCatalogFromAssets(context, "db.json")
        catalogData.value = data
        isLoading.value = false
    }


    CompositionLocalProvider(
        LocalCatalogData provides catalogData.value
    ) {
        when {
            isLoading.value -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            !isLoading.value -> {
                AppNavigation()
            }
        }

    }
}
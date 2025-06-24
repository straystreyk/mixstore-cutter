package com.pokrikinc.mixpokrikcutter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.pokrikinc.mixpokrikcutter.data.model.Category
import com.pokrikinc.mixpokrikcutter.data.repository.CatalogRepository
import com.pokrikinc.mixpokrikcutter.plotter.DeviceManager
import com.pokrikinc.mixpokrikcutter.plotter.LogUtils
import com.pokrikinc.mixpokrikcutter.ui.navigation.AppNavigation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

// Композиционные локали
val LocalCatalogData = compositionLocalOf<List<Category>> { emptyList() }
val LocalImagesData = compositionLocalOf { JsonObject() }
val LocalDeviceInstance = compositionLocalOf<DeviceManager?> { null }

// Состояния загрузки
private enum class LoadingState {
    LOADING,
    DEVICE_ERROR,
    DATA_ERROR,
    SUCCESS
}

@Composable
fun PokrikApp() {
    val context = LocalContext.current

    // Состояния
    var loadingState by remember { mutableStateOf(LoadingState.LOADING) }
    var deviceManager by remember { mutableStateOf<DeviceManager?>(null) }
    var catalogData by remember { mutableStateOf<List<Category>>(emptyList()) }
    var images by remember { mutableStateOf(JsonObject()) }
    var loadingMessage by remember { mutableStateOf("Инициализация...") }

    // Функция загрузки данных
    suspend fun loadAppData() {
        try {
            loadingMessage = "Загрузка каталога..."

            // Параллельная загрузка данных в coroutineScope
            coroutineScope {
                val catalogDeferred = async {
                    CatalogRepository.loadCatalogFromAssets(context, "db.json")
                }
                val imagesDeferred = async {
                    val imagesString = CatalogRepository.loadFile(context, "images.json")
                    Gson().fromJson(imagesString, JsonObject::class.java)
                }

                // Ожидаем загрузку данных
                catalogData = catalogDeferred.await()
                images = imagesDeferred.await()
            }

            if (catalogData.isEmpty()) {
                loadingState = LoadingState.DATA_ERROR
                return
            }

            loadingMessage = "Подключение к плоттеру..."

            // Инициализация устройства
            try {
                deviceManager = DeviceManager.getInstance()
                deviceManager?.start485()
                loadingState = LoadingState.SUCCESS
            } catch (e: Exception) {
                LogUtils.e("AppLaunch", "Device connection error: ${e.message}")
                loadingState = LoadingState.DEVICE_ERROR
            }

        } catch (e: Exception) {
            LogUtils.e("AppLaunch", "Data loading error: ${e.message}")
            loadingState = LoadingState.DATA_ERROR
        }
    }

    // Загружаем данные при старте
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            loadAppData()
        }
    }

    // Провайдеры данных
    CompositionLocalProvider(
        LocalCatalogData provides catalogData,
        LocalImagesData provides images,
        LocalDeviceInstance provides deviceManager
    ) {
        when (loadingState) {
            LoadingState.LOADING -> {
                LoadingScreen(message = loadingMessage)
            }

            LoadingState.DEVICE_ERROR -> {
                ErrorScreen(
                    title = "Ошибка подключения",
                    message = "Не удалось подключиться к плоттеру. Проверьте соединение.",
                    showRetry = true,
                    onRetry = {
                        loadingState = LoadingState.LOADING
                        loadingMessage = "Повторное подключение..."
                        // Запускаем повторную попытку
                    }
                )
            }

            LoadingState.DATA_ERROR -> {
                ErrorScreen(
                    title = "Ошибка загрузки данных",
                    message = "Не удалось загрузить каталог. Проверьте файлы приложения.",
                    showRetry = false
                )
            }

            LoadingState.SUCCESS -> {
                AppNavigation()
            }
        }
    }
}

// Компонент экрана загрузки
@Composable
private fun LoadingScreen(
    message: String
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// Компонент экрана ошибки
@Composable
private fun ErrorScreen(
    title: String,
    message: String,
    showRetry: Boolean = false,
    onRetry: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (showRetry && onRetry != null) {
                androidx.compose.material3.Button(
                    onClick = onRetry
                ) {
                    Text("Повторить")
                }
            }
        }
    }
}
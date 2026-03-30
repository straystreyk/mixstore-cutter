package com.pokrikinc.mixpokrikcutter.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.pokrikinc.mixpokrikcutter.data.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.util.zip.GZIPInputStream
import java.util.concurrent.ConcurrentHashMap

/**
 * 🚀 ОПТИМИЗИРОВАННЫЙ РЕПОЗИТОРИЙ С КЭШИРОВАНИЕМ И АСИНХРОННОСТЬЮ
 * Критичные оптимизации для старых устройств:
 * 1. Ленивая загрузка данных
 * 2. Кэширование в памяти
 * 3. Асинхронная работа с файлами
 * 4. Переиспользование Gson инстанса
 */
object CatalogRepository {

    // 🚀 КРИТИЧНО: Кэш для избежания повторного парсинга JSON
    private val fileCache = ConcurrentHashMap<String, String>()
    private val catalogCache = ConcurrentHashMap<String, List<Category>>()
    private val jsonObjectCache = ConcurrentHashMap<String, JsonObject>()

    // 🚀 КРИТИЧНО: Переиспользуем Gson инстанс (экономия памяти)
    private val gson by lazy { Gson() }

    /**
     * 🚀 ОПТИМИЗИРОВАННАЯ загрузка файла с кэшированием
     */
    suspend fun loadFile(context: Context, fileName: String): String = withContext(Dispatchers.IO) {
        // Проверяем кэш первым делом
        fileCache[fileName]?.let { return@withContext it }

        try {
            val content = readAssetText(context, fileName)
            // Кэшируем результат
            fileCache[fileName] = content
            content
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    private fun readAssetText(context: Context, fileName: String): String {
        val assetNames = context.assets.list("")?.toSet().orEmpty()
        val resolvedName = when {
            fileName in assetNames -> fileName
            "$fileName.gz" in assetNames -> "$fileName.gz"
            else -> fileName
        }

        return context.assets.open(resolvedName).use { input ->
            val stream = if (resolvedName.endsWith(".gz")) GZIPInputStream(input) else input
            stream.bufferedReader().use { it.readText() }
        }
    }

    /**
     * 🚀 ОПТИМИЗИРОВАННАЯ загрузка каталога с кэшированием
     */
    suspend fun loadCatalogFromAssets(context: Context, fileName: String): List<Category> =
        withContext(Dispatchers.IO) {
            // Проверяем кэш каталога
            catalogCache[fileName]?.let { return@withContext it }

            try {
                val jsonString = loadFile(context, fileName)
                if (jsonString.isEmpty()) return@withContext emptyList()

                val type = object : TypeToken<List<Category>>() {}.type
                val catalog = gson.fromJson<List<Category>>(jsonString, type) ?: emptyList()

                // Кэшируем результат
                catalogCache[fileName] = catalog
                catalog
            } catch (e: Exception) {
                e.printStackTrace()
                emptyList()
            }
        }

    /**
     * 🚀 НОВАЯ: Оптимизированная загрузка JsonObject с кэшированием
     */
    suspend fun loadJsonObject(context: Context, fileName: String): JsonObject =
        withContext(Dispatchers.IO) {
            // Проверяем кэш JsonObject
            jsonObjectCache[fileName]?.let { return@withContext it }

            try {
                val jsonString = loadFile(context, fileName)
                if (jsonString.isEmpty()) return@withContext JsonObject()

                val jsonObject = gson.fromJson(jsonString, JsonObject::class.java) ?: JsonObject()

                // Кэшируем результат
                jsonObjectCache[fileName] = jsonObject
                jsonObject
            } catch (e: Exception) {
                e.printStackTrace()
                JsonObject()
            }
        }

    /**
     * 🚀 ИСПРАВЛЕННАЯ: Предзагрузка критичных файлов
     */
    suspend fun preloadCriticalFiles(context: Context) = withContext(Dispatchers.IO) {
        try {
            // 🚀 ИСПРАВЛЕНО: Используем coroutineScope для параллельной загрузки
            coroutineScope {
                val dbDeferred = async { loadFile(context, "db.json") }
                val imagesDeferred = async { loadFile(context, "images.json") }

                // Ждем завершения загрузки (результаты кэшируются)
                dbDeferred.await()
                imagesDeferred.await()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 🚀 НОВАЯ: Очистка кэша для освобождения памяти
     */
    fun clearCache() {
        fileCache.clear()
        catalogCache.clear()
        jsonObjectCache.clear()
    }

    /**
     * 🚀 НОВАЯ: Получение размера кэша для мониторинга
     */
    fun getCacheInfo(): String {
        return "Files: ${fileCache.size}, Catalogs: ${catalogCache.size}, JsonObjects: ${jsonObjectCache.size}"
    }
}

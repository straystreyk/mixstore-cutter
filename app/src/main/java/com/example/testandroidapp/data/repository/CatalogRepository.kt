package com.example.testandroidapp.data.repository

import android.content.Context
import com.example.testandroidapp.data.model.Category
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object CatalogRepository {
    /**
     * Загружает каталог из assets
     * @param context Контекст приложения
     * @param fileName Имя файла в assets (по умолчанию "db.json")
     * @return Список категорий или пустой список в случае ошибки
     */
    fun loadCatalogFromAssets(context: Context, fileName: String): List<Category> {
        try {
            val jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val type = object : TypeToken<List<Category>>() {}.type
            return Gson().fromJson(jsonString, type) ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }
    }
}
package com.pokrikinc.mixpokrikcutter.data.repository

import com.pokrikinc.mixpokrikcutter.data.model.RemoteCatalogCategory
import com.pokrikinc.mixpokrikcutter.data.model.RemoteCatalogPart
import com.pokrikinc.mixpokrikcutter.data.remote.ICustomCatalogApi

class CustomCatalogRepository(private val api: ICustomCatalogApi) {
    data class PartsPage(
        val items: List<RemoteCatalogPart>,
        val isLastPage: Boolean,
        val page: Int
    )

    suspend fun loadCategories(): List<RemoteCatalogCategory> {
        return api.getCategories()
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
            .map { categoryName ->
                RemoteCatalogCategory(
                    id = slugify(categoryName),
                    name = categoryName,
                    imageUrl = null
                )
            }
    }

    suspend fun loadPartsPage(
        baseUrl: String,
        categoryName: String,
        page: Int,
        filter: String
    ): PartsPage {
        val response = api.getParts(
            page = page,
            size = PAGE_SIZE,
            filter = filter,
            categories = listOf(categoryName)
        )

        return PartsPage(
            items = response.content.map { part ->
                RemoteCatalogPart(
                    id = part.id.toString(),
                    title = part.partName,
                    subtitle = "${part.vendor} • ${part.device}",
                    meta = "${part.category} / ${part.vendor} / ${part.device}",
                    imageUrl = "$baseUrl/files/${part.id}/partImgData",
                    remoteId = part.id
                )
            },
            isLastPage = response.last,
            page = response.number
        )
    }

    private fun slugify(value: String): String {
        return value
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
            .ifBlank { "item" }
    }

    private companion object {
        private const val PAGE_SIZE = 40
    }
}

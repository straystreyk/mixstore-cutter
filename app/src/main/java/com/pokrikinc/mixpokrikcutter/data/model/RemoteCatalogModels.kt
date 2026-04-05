package com.pokrikinc.mixpokrikcutter.data.model

data class RemotePartDto(
    val id: Int,
    val category: String,
    val vendor: String,
    val device: String,
    val partName: String
)

data class RemoteCatalogCategory(
    val id: String,
    val name: String,
    val imageUrl: String?
)

data class RemoteCatalogPart(
    val id: String,
    val title: String,
    val subtitle: String,
    val meta: String,
    val imageUrl: String?,
    val remoteId: Int
)

package com.pokrikinc.mixpokrikcutter.data.model

data class PageResponse<T>(
    val content: List<T> = emptyList(),
    val last: Boolean = true,
    val number: Int = 0,
    val totalPages: Int = 0
)

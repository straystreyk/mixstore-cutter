package com.pokrikinc.mixpokrikcutter.data.model

data class Category(
    val id: String,
    val name: String,
    val img: String,
    val vendors: List<Vendor>
)
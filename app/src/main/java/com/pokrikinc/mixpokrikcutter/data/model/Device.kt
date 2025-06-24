package com.pokrikinc.mixpokrikcutter.data.model

data class Device(
    val id: String,
    val name: String,
    val partlist: List<Part>
)
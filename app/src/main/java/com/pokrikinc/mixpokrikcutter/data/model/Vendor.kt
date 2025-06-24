package com.pokrikinc.mixpokrikcutter.data.model

data class Vendor(
    val id: String,
    val name: String,
    val img: String,
    val devices: List<Device>
)
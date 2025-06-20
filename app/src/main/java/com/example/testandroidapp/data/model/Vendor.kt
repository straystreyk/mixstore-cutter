package com.example.testandroidapp.data.model

data class Vendor(
    val id: String,
    val name: String,
    val img: String,
    val devices: List<Device>
)
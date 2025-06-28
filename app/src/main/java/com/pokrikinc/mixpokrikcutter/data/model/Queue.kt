package com.pokrikinc.mixpokrikcutter.data.model

data class Queue(
    val id: Int,
    val name: String,
    val orders: List<Order>? = null
)

data class Order(
    var id: Int = 0,
    val barcode: String = "",
    var name: String = "",
    val qrCodePageIndex: Int? = null,
    val qrCodePage: String? = null,
    val barcodePageIndex: Int = 0,
    val barcodePage: String = "",
    var parts: List<Part>? = null,
    var isPrinted: Boolean = false
)
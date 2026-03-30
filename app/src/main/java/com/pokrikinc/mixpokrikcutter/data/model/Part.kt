package com.pokrikinc.mixpokrikcutter.data.model

data class Part(
    val name: String = "",
    val picfile: String = "",
    val attfile: String = "",
    val partId: Int? = null,
    val cutData: String? = null,
    var isPrinted: Boolean = false
)

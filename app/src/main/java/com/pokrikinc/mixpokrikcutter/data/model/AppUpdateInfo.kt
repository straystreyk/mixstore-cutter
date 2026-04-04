package com.pokrikinc.mixpokrikcutter.data.model

data class AppUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val mandatory: Boolean = false,
    val notes: String? = null
)

package com.pokrikinc.mixpokrikcutter.data.remote

import com.pokrikinc.mixpokrikcutter.data.model.AppUpdateInfo
import retrofit2.http.GET
import retrofit2.http.Url

interface IAppUpdateApi {
    @GET
    suspend fun getUpdateInfo(@Url url: String): AppUpdateInfo
}

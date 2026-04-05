package com.pokrikinc.mixpokrikcutter.data.remote

import com.pokrikinc.mixpokrikcutter.data.model.PageResponse
import com.pokrikinc.mixpokrikcutter.data.model.RemotePartDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ICustomCatalogApi {
    @GET("parts/categories")
    suspend fun getCategories(): List<String>

    @GET("parts")
    suspend fun getParts(
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("filter") filter: String = "",
        @Query("categories") categories: List<String>
    ): PageResponse<RemotePartDto>

    @GET("files/{id}/partCutData")
    suspend fun getPartCutData(@Path("id") id: Int): ResponseBody
}

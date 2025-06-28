package com.pokrikinc.mixpokrikcutter.data.remote

import com.pokrikinc.mixpokrikcutter.data.model.Queue
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface IPlotterApi {
    @GET("cutter/queue/{id}")
    suspend fun getQueue(@Path("id") id: Int): Queue

    @GET("cutter/queues")
    suspend fun listQueues(): List<Queue>

    @GET("cutter/print-sticker")
    suspend fun printSticker(
        @Query("queueId") queueId: Int,
        @Query("orderId") orderId: Int,
        @Query("printerName") printerName: String
    ): ResponseBody
}
package com.pokrikinc.mixpokrikcutter.data.repository

import com.pokrikinc.mixpokrikcutter.data.model.Queue
import com.pokrikinc.mixpokrikcutter.data.remote.IPlotterApi
import okhttp3.ResponseBody


class PlotterRepository(private val api: IPlotterApi) {
    suspend fun getQueue(id: Int): Queue {
        return api.getQueue(id)
    }

    suspend fun listQueues(): List<Queue> {
        return api.listQueues()
    }

    suspend fun printSticker(queueId: Int, orderId: Int, printerName: String): ResponseBody {
        return api.printSticker(queueId, orderId, printerName)
    }
}
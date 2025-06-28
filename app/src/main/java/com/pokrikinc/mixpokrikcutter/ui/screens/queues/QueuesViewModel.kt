package com.pokrikinc.mixpokrikcutter.ui.screens.queues

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.model.Queue

class QueuesViewModel : ViewModel() {
    val api = RetrofitProvider.getPlotterApi()
    var queues by mutableStateOf<List<Queue>>(emptyList())

    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    suspend fun loadQueues() {
        isLoading = true
        try {
            queues = api.listQueues()
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }
}

package com.pokrikinc.mixpokrikcutter.ui.screens.queues

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.model.Queue

class QueuesViewModel : ViewModel() {
    val api = RetrofitProvider.getPlotterApi()
    var queues = mutableStateListOf<Queue>()

    var isLoading by mutableStateOf(false)

    var errorMessage by mutableStateOf<String?>(null)

    var searchQuery by mutableStateOf("")

    val filteredQueues: List<Queue>
        get() = if (searchQuery.isBlank()) {
            queues
        } else {
            queues.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }

    suspend fun loadQueues() {
        isLoading = true
        try {
            val result = api.listQueues()
            queues.clear()
            queues.addAll(result)
        } catch (e: Exception) {
            errorMessage = e.message
        } finally {
            isLoading = false
        }
    }
}

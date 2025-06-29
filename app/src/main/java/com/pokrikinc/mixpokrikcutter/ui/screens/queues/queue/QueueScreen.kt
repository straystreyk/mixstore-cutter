package com.pokrikinc.mixpokrikcutter.ui.screens.queues.queue

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokrikinc.mixpokrikcutter.data.RetrofitProvider
import com.pokrikinc.mixpokrikcutter.data.model.Order
import com.pokrikinc.mixpokrikcutter.data.model.Queue
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalTitleViewModel
import kotlinx.coroutines.launch

// ViewModel для страницы очереди
class QueueViewModel : ViewModel() {
    val api = RetrofitProvider.getPlotterApi()

    var queue by mutableStateOf<Queue?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadQueue(id: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                queue = api.getQueue(id)
            } catch (e: Exception) {
                errorMessage = "Ошибка загрузки очереди: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}

@Composable
fun QueueScreen(queueId: Int, viewModel: QueueViewModel = viewModel()) {
    val titleViewModel = LocalTitleViewModel.current
    val queue = viewModel.queue
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(queueId) {
        viewModel.loadQueue(queueId)
    }

    LaunchedEffect(queue) {
        if (queue !== null) {
            titleViewModel.setTitle(queue.name)
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            errorMessage != null -> {
                Text(text = errorMessage, color = Color.Red)
            }

            queue != null -> {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    QueueDetails(queue)
                }

            }

            else -> {
                Text("Очередь не найдена")
            }
        }
    }
}

@Composable
fun QueueDetails(queue: Queue) {
    Card {
        Column(modifier = Modifier.padding(8.dp)) {
            if (queue.orders.isNullOrEmpty()) {
                Text("Заказы отсутствуют")
            } else {
                // Список заказов
                queue.orders.forEach { order ->
                    OrderItem(order)
                    Spacer(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

    }
}


@Composable
fun OrderItem(order: Order) {
    Text(text = "Заказ ID: ${order.id}", style = MaterialTheme.typography.titleSmall)
    Text(text = "Название: ${order.name}")
    Row {
        Text(
            text = "Напечатан: "
        )
        if (order.isPrinted) Icon(
            Icons.Default.Check,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Check"
        ) else Icon(
            Icons.Default.Close,
            tint = MaterialTheme.colorScheme.error,
            contentDescription = "Close"
        )
    }

    Text("${order.parts?.size}")
}
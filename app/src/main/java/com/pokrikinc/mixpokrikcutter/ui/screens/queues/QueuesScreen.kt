package com.pokrikinc.mixpokrikcutter.ui.screens.queues

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pokrikinc.mixpokrikcutter.data.model.Queue
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalNavController


@Composable
fun QueuesScreen(viewModel: QueuesViewModel = viewModel()) {
    val queues = viewModel.queues
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.loadQueues()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

            }

            errorMessage != null -> {
                Text(
                    text = "Ошибка!! Попробуйте заменить URL адрес в настройках",
                    color = Color.Red,
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(queues.size) { index ->
                        val item = queues[index]
                        QueueItem(item)
                    }
                }
            }
        }
    }
}

@Composable
fun QueueItem(queue: Queue) {
    val navController = LocalNavController.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        onClick = {
            navController.navigate("queues/${queue.id}")
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "ID: ${queue.id}", style = MaterialTheme.typography.titleSmall)
            Text(text = "Название: ${queue.name}", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

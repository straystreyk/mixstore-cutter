package com.pokrikinc.mixpokrikcutter.ui.screens.queues

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.pokrikinc.mixpokrikcutter.ui.navigation.LocalTitleViewModel

@Composable
fun QueuesScreen(viewModel: QueuesViewModel = viewModel()) {
    val queues = viewModel.filteredQueues
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val searchQuery = viewModel.searchQuery
    val titleViewModel = LocalTitleViewModel.current

    LaunchedEffect(Unit) {
        titleViewModel.setTitle("Очереди")
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {

                    // 🔍 Поле поиска
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery = it },
                        label = { Text("Поиск по названию") },
                        singleLine = true
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp)
                    ) {
                        items(queues, key = { it.id }) { item ->
                            QueueItem(item)
                        }
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
            .padding(vertical = 2.dp),
        onClick = {
            navController.navigate("queues/${queue.id}")
        },
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "ID: ${queue.id}",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                text = "Название: ${queue.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
fun LoadingView() {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorView(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ошибка: $message",
            color = Color.Red,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

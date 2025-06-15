package com.example.testandroidapp.ui.screens.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testandroidapp.data.model.UserDto

import com.example.testandroidapp.data.repository.UserRepository
import kotlinx.coroutines.launch

@Composable
fun MainScreen(navController: NavController) {
    val repository = UserRepository()
    val scope = rememberCoroutineScope()

    // Состояние списка пользователей, изначально пустой список
    var users by remember { mutableStateOf<List<UserDto>>(emptyList()) }

    fun loadUsers() {
        scope.launch {
            try {
                users = repository.getUsers()
            } catch (e: Exception) {
                println("Ошибка загрузки: ${e.message}")
            }
        }
    }

    LaunchedEffect(Unit) {
        loadUsers()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Список пользователей", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(users.size) { index ->
                val user = users[index]
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    onClick = { navController.navigate("user/${user.id}") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .padding(vertical = 4.dp)
                        .clickable {
                            val selectedUserName = user.name
                            // Здесь можно, например, сделать навигацию или показать Toast
                            println("Кликнули на пользователя: ${selectedUserName}")
                        },
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(text = user.name, style = MaterialTheme.typography.bodyLarge)
                        Text(text = user.email, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}



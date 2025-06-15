package com.example.testandroidapp.ui.screens.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.testandroidapp.data.model.UserDto
import com.example.testandroidapp.data.repository.UserRepository
import kotlinx.coroutines.launch

@Composable
fun UserDetailScreen(
    navController: NavController,
    userId: Long,
) {
    val repository = UserRepository()
    val scope = rememberCoroutineScope()

    var user by remember { mutableStateOf<UserDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit, userId) {
        scope.launch {
            try {
                isLoading = true
                if (userId != -1L) {
                    user = repository.getUserById(userId)
                }
            } catch (e: Exception) {
                println("Ошибка загрузки: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        when {
            isLoading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }

            user != null -> {
                Text("Имя: ${user?.name}", style = MaterialTheme.typography.headlineSmall)
                Text("Email: ${user?.email}", style = MaterialTheme.typography.bodyMedium)
                Text(buildAnnotatedString {
                    append("Website: ")
                    withLink(
                        LinkAnnotation.Url(
                            "https://tskanyan.ru",
                            TextLinkStyles(style = SpanStyle(color = Color.Blue))
                        )
                    ) {
                        append(user?.website)
                    }
                }, style = MaterialTheme.typography.bodyMedium)
            }

            else -> {
                Text("Данные пользователя не найдены")
            }
        }
    }

}
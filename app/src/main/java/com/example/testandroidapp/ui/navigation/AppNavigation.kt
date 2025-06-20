package com.example.testandroidapp.ui.navigation

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.testandroidapp.ui.screens.category.CategoryScreen
import com.example.testandroidapp.ui.screens.main.MainScreen

const val defaultTitle = "MixCutter"

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentTitle = navBackStackEntry?.arguments?.getString("title") ?: defaultTitle

    CompositionLocalProvider(
        LocalNavController provides navController
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = {
                        Text(text = currentTitle)
                    },
                    navigationIcon = {
                        if (navController.previousBackStackEntry != null) {
                            IconButton(
                                onClick = { navController.navigateUp() },
                            ) {
                                Icon(
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    },
                )
            },
            bottomBar = {
                BottomAppBar(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    content = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            IconButton(onClick = { navController.navigate("catalog") }) {
                                Icon(Icons.Default.Home, contentDescription = "Home")
                            }
                            IconButton(onClick = {
                                val intent = Intent(Settings.ACTION_SETTINGS)
                                context.startActivity(intent)
                            }) {
                                Icon(Icons.Default.Settings, contentDescription = "Profile")
                            }
                        }
                    },
                )
            }
        ) { padding ->
            NavHost(
                navController = navController,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None },
                popExitTransition = { ExitTransition.None },
                startDestination = "catalog",
                modifier = Modifier.padding(padding)
            ) {
                composable(
                    "catalog"
                ) {
                    MainScreen()
                }
                composable(
                    "catalog/{categoryId}",
                    arguments = listOf(
                        navArgument("categoryId") {
                            type = NavType.StringType
                        }
                    )
                ) { navBackStackEntry ->
                    val categoryId = navBackStackEntry.arguments?.getString("categoryId")
                    CategoryScreen(categoryId)
                }
            }
        }
    }
}

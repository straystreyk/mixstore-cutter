package com.pokrikinc.mixpokrikcutter.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pokrikinc.mixpokrikcutter.ui.screens.device.DeviceScreen
import com.pokrikinc.mixpokrikcutter.ui.screens.main.MainScreen
import com.pokrikinc.mixpokrikcutter.ui.screens.parts.PartsScreen
import com.pokrikinc.mixpokrikcutter.ui.screens.queues.QueuesScreen
import com.pokrikinc.mixpokrikcutter.ui.screens.queues.queue.QueueScreen
import com.pokrikinc.mixpokrikcutter.ui.screens.vendor.VendorScreen
import net.ezcoder.ezprinter.ui.settings.SettingsScreen

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}
val LocalTitleViewModel = staticCompositionLocalOf<TitleViewModel> {
    error("TitleViewModel not provided")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val titleViewModel: TitleViewModel = viewModel()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val hasPreviousDestination by remember(navBackStackEntry) {
        derivedStateOf { navController.previousBackStackEntry != null }
    }
    val title by titleViewModel.title.collectAsState()


    CompositionLocalProvider(
        LocalNavController provides navController,
        LocalTitleViewModel provides titleViewModel,
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    title = {
                        Text(text = title)
                    },
                    navigationIcon = {
                        if (hasPreviousDestination) {
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
                            IconButton(onClick = { navController.navigate("queues") }) {
                                Icon(Icons.Default.Menu, contentDescription = "Queues")
                            }
                            IconButton(onClick = {
                                navController.navigate("settings")
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
                composable("catalog") {
                    MainScreen()
                }
                composable("queues") {
                    QueuesScreen()
                }
                composable(
                    route = "queues/{queueId}",
                    arguments = listOf(navArgument("queueId") { type = NavType.IntType })
                ) { backStackEntry ->
                    val queueId = backStackEntry.arguments?.getInt("queueId") ?: 0

                    QueueScreen(queueId = queueId)
                }
                composable("settings") {
                    SettingsScreen()
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
                    VendorScreen(categoryId)
                }
                composable(
                    "catalog/{categoryId}/{vendorId}",
                    arguments = listOf(
                        navArgument("categoryId") { type = NavType.StringType },
                        navArgument("vendorId") { type = NavType.StringType }
                    )
                ) { navBackStackEntry ->
                    val categoryId = navBackStackEntry.arguments?.getString("categoryId")
                    val vendorId = navBackStackEntry.arguments?.getString("vendorId")
                    DeviceScreen(categoryId, vendorId)
                }
                composable(
                    "catalog/{categoryId}/{vendorId}/{deviceId}",
                    arguments = listOf(
                        navArgument("categoryId") { type = NavType.StringType },
                        navArgument("vendorId") { type = NavType.StringType },
                        navArgument("deviceId") { type = NavType.StringType }
                    )
                ) { navBackStackEntry ->
                    val categoryId = navBackStackEntry.arguments?.getString("categoryId")
                    val vendorId = navBackStackEntry.arguments?.getString("vendorId")
                    val deviceId = navBackStackEntry.arguments?.getString("deviceId")

                    PartsScreen(categoryId, vendorId, deviceId)
                }
            }
        }
    }
}

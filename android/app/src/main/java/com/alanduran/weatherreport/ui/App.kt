package com.alanduran.weatherreport.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.alanduran.weatherreport.ui.components.WeatherBackground
import com.alanduran.weatherreport.ui.screens.AirQualityScreen
import com.alanduran.weatherreport.ui.screens.MainScreen
import com.alanduran.weatherreport.ui.screens.SettingsScreen
import com.alanduran.weatherreport.ui.theme.TextTertiary
import com.alanduran.weatherreport.ui.theme.VioletAccent
import com.alanduran.weatherreport.ui.theme.VioletBright

private const val ROUTE_MAIN = "main"
private const val ROUTE_AIR = "air"
private const val ROUTE_SETTINGS = "settings"

@Composable
fun WeatherApp() {
    val viewModel: WeatherViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    WeatherBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (currentRoute == ROUTE_MAIN || currentRoute == ROUTE_AIR) {
                    WeatherBottomBar(
                        currentRoute = currentRoute,
                        onSelect = { route ->
                            navController.navigate(route) {
                                popUpTo(ROUTE_MAIN) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = ROUTE_MAIN,
                modifier = Modifier.padding(padding)
            ) {
                composable(ROUTE_MAIN) {
                    MainScreen(
                        state = state,
                        onRefresh = viewModel::refresh,
                        onOpenAirQuality = { navController.navigate(ROUTE_AIR) },
                        onOpenSettings = { navController.navigate(ROUTE_SETTINGS) }
                    )
                }
                composable(ROUTE_AIR) {
                    AirQualityScreen(
                        state = state,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(ROUTE_SETTINGS) {
                    SettingsScreen(
                        settings = settings,
                        onSave = viewModel::saveSettings,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun WeatherBottomBar(currentRoute: String?, onSelect: (String) -> Unit) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.06f),
        tonalElevation = 0.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == ROUTE_MAIN,
            onClick = { onSelect(ROUTE_MAIN) },
            icon = { Text("\u2601\uFE0F", fontSize = 18.sp) },
            label = { Text("Clima") },
            colors = weatherNavColors()
        )
        NavigationBarItem(
            selected = currentRoute == ROUTE_AIR,
            onClick = { onSelect(ROUTE_AIR) },
            icon = { Text("\uD83C\uDF2B\uFE0F", fontSize = 18.sp) },
            label = { Text("Calidad del aire") },
            colors = weatherNavColors()
        )
    }
}

@Composable
private fun weatherNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = VioletBright,
    selectedTextColor = VioletBright,
    indicatorColor = VioletAccent.copy(alpha = 0.25f),
    unselectedIconColor = TextTertiary,
    unselectedTextColor = TextTertiary
)

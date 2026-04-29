package com.nanoorbit.groundcontrol.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.nanoorbit.groundcontrol.ui.screens.dashboard.DashboardScreen
import com.nanoorbit.groundcontrol.ui.screens.detail.DetailScreen
import com.nanoorbit.groundcontrol.ui.screens.map.MapScreen
import com.nanoorbit.groundcontrol.ui.screens.planning.PlanningScreen

@Composable
fun NanoOrbitNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute != Routes.DETAIL && currentRoute?.startsWith("detail/") != true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    val tabs = listOf(
                        Routes.DASHBOARD to "Dashboard",
                        Routes.PLANNING to "Planning",
                        Routes.MAP to "Carte"
                    )
                    tabs.forEach { (route, label) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(label) },
                            icon = {}
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DASHBOARD,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Routes.DASHBOARD) {
                DashboardScreen(
                    onSatelliteClick = { satellite ->
                        navController.navigate(Routes.detail(satellite.idSatellite))
                    }
                )
            }

            composable(Routes.PLANNING) {
                PlanningScreen()
            }

            composable(Routes.MAP) {
                MapScreen()
            }

            composable(
                route = Routes.DETAIL,
                arguments = listOf(navArgument("satelliteId") { type = NavType.StringType })
            ) { backStackEntry ->
                val satelliteId = backStackEntry.arguments?.getString("satelliteId").orEmpty()
                DetailScreen(
                    satelliteId = satelliteId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

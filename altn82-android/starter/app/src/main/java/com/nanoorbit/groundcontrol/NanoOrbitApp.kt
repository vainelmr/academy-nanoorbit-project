package com.nanoorbit.groundcontrol

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.nanoorbit.groundcontrol.ui.screens.dashboard.DashboardScreen
import com.nanoorbit.groundcontrol.ui.theme.NanoOrbitTheme

@Composable
fun NanoOrbitApp() {
    NanoOrbitTheme {
        Scaffold { innerPadding ->
            DashboardScreen(modifier = Modifier.padding(innerPadding))
        }
    }
}

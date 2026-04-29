package com.nanoorbit.groundcontrol

import androidx.compose.runtime.Composable
import com.nanoorbit.groundcontrol.navigation.NanoOrbitNavigation
import com.nanoorbit.groundcontrol.ui.theme.NanoOrbitTheme

@Composable
fun NanoOrbitApp() {
    NanoOrbitTheme {
        NanoOrbitNavigation()
    }
}

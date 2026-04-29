package com.nanoorbit.groundcontrol.navigation

object Routes {
    const val DASHBOARD = "dashboard"
    const val PLANNING = "planning"
    const val MAP = "map"
    const val DETAIL = "detail/{satelliteId}"

    fun detail(satelliteId: String) = "detail/$satelliteId"
}

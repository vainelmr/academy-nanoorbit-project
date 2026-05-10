package com.nanoorbit.groundcontrol.work

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

private const val CHANNEL_ID = "planning_windows"

internal fun notificationChannelId(): String = CHANNEL_ID

fun ensurePlanningNotificationsChannel(application: Application) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
    val mgr = application.getSystemService(NotificationManager::class.java) ?: return
    mgr.createNotificationChannel(
        NotificationChannel(
            CHANNEL_ID,
            "Planning communications",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Rappels liés aux fenêtres PLANIFIÉES"
        }
    )
}

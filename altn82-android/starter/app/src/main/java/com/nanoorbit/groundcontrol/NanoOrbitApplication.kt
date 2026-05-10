package com.nanoorbit.groundcontrol

import android.app.Application
import com.nanoorbit.groundcontrol.work.ensurePlanningNotificationsChannel

class NanoOrbitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ensurePlanningNotificationsChannel(this)
    }
}

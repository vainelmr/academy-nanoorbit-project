package com.nanoorbit.groundcontrol.work

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class PlanningWindowNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val idSat = inputData.getString(KEY_ID_SATELLITE).orEmpty()
        val nomSat = inputData.getString(KEY_NOM_SATELLITE).orEmpty().ifBlank { idSat }
        val codeStation = inputData.getString(KEY_CODE_STATION).orEmpty()
        val nomStation =
            inputData.getString(KEY_NOM_STATION).orEmpty().ifBlank { codeStation }
        val dureeSec = inputData.getInt(KEY_DUREE, 0)

        val mgr = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, notificationChannelId())
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Communication planifiée (dans ~15 min)")
            .setContentText("$nomSat ↔ $nomStation • ${dureeSec}s")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        """
                        Satellite : $nomSat ($idSat)
                        Station : $nomStation ($codeStation)
                        Durée : ${dureeSec}s
                        """.trimIndent()
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        mgr.notify(idSat.hashCode(), notification)
        return Result.success()
    }

    companion object {
        const val KEY_ID_SATELLITE = "id_satellite"
        const val KEY_NOM_SATELLITE = "nom_satellite"
        const val KEY_CODE_STATION = "code_station"
        const val KEY_NOM_STATION = "nom_station"
        const val KEY_DUREE = "duree"

        const val WORK_TAG = "planning_fenetre_notif"
    }
}

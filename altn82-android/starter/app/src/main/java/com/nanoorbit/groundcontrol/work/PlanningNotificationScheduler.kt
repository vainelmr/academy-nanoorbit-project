package com.nanoorbit.groundcontrol.work

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.data.models.FenetreCom
import com.nanoorbit.groundcontrol.data.models.StatutFenetre
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object PlanningNotificationScheduler {

    /** Replanifie les rappels 15 min avant chaque fenêtre PLANIFIÉE encore dans le futur. */
    fun reschedule(context: Context, fenetres: List<FenetreCom>, satelliteNamesById: Map<String, String>) {
        val appContext = context.applicationContext
        val wm = WorkManager.getInstance(appContext)
        wm.cancelAllWorkByTag(PlanningWindowNotificationWorker.WORK_TAG)

        val now = Instant.now()
        val zone = ZoneId.systemDefault()
        val stationsByCode = MockData.stations.associateBy { it.codeStation }

        fenetres.filter { it.statut == StatutFenetre.PLANIFIEE }.forEach { f ->
            val notifyAt = f.datetimeDebut
                .atZone(zone)
                .toInstant()
                .minus(15, ChronoUnit.MINUTES)
            val delayMs = java.time.Duration.between(now, notifyAt).toMillis()
            if (delayMs < 60_000L) return@forEach

            val input = Data.Builder()
                .putString(PlanningWindowNotificationWorker.KEY_ID_SATELLITE, f.idSatellite)
                .putString(
                    PlanningWindowNotificationWorker.KEY_NOM_SATELLITE,
                    satelliteNamesById[f.idSatellite] ?: f.idSatellite
                )
                .putString(PlanningWindowNotificationWorker.KEY_CODE_STATION, f.codeStation)
                .putString(
                    PlanningWindowNotificationWorker.KEY_NOM_STATION,
                    stationsByCode[f.codeStation]?.nomStation ?: f.codeStation
                )
                .putInt(PlanningWindowNotificationWorker.KEY_DUREE, f.duree)
                .build()

            val request = OneTimeWorkRequestBuilder<PlanningWindowNotificationWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(input)
                .addTag(PlanningWindowNotificationWorker.WORK_TAG)
                .build()

            wm.enqueueUniqueWork(
                "${PlanningWindowNotificationWorker.WORK_TAG}_${f.idFenetre}",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}

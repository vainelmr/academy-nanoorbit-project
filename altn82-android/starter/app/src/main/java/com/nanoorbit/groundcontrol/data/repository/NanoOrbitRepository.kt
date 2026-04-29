package com.nanoorbit.groundcontrol.data.repository

import com.nanoorbit.groundcontrol.data.local.FenetreDao
import com.nanoorbit.groundcontrol.data.local.toDomain
import com.nanoorbit.groundcontrol.data.local.toEntity
import com.nanoorbit.groundcontrol.data.local.SatelliteDao
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.data.models.FenetreCom
import com.nanoorbit.groundcontrol.data.models.Satellite
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Debug uniquement : passer à `true` pour simuler une panne serveur / réseau
 * avant l’écriture Room (les données déjà en cache restent affichées).
 */
private const val SIMULATE_NETWORK_FAILURE = false

class NanoOrbitRepository(
    private val satelliteDao: SatelliteDao,
    private val fenetreDao: FenetreDao
) {
    // Lien ALTN83 Q3 : la stratégie Cache-First permet de continuer à consulter/planifier
    // avec les dernières données locales même si le serveur central est indisponible.

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun observeSatellites(): Flow<List<Satellite>> =
        satelliteDao.getAllSatellites().map { list -> list.map { it.toDomain() } }

    fun observeFenetres(): Flow<List<FenetreCom>> =
        fenetreDao.getAllFenetres().map { list -> list.map { it.toDomain() } }

    fun observeLatestCacheTimestamp(): Flow<Long?> = satelliteDao.getLatestCacheTimestamp()

    suspend fun refreshSatellites() {
        try {
            delay(500)
            if (SIMULATE_NETWORK_FAILURE) {
                throw IOException("Simulation serveur indisponible")
            }
            val now = System.currentTimeMillis()
            satelliteDao.upsertAll(MockData.satellites.map { it.toEntity(now) })
            _isOfflineMode.value = false
            _errorMessage.value = null
        } catch (e: Exception) {
            _isOfflineMode.value = true
            _errorMessage.value = "Données en cache: source distante indisponible"
        }
    }

    suspend fun refreshFenetres() {
        try {
            delay(500)
            if (SIMULATE_NETWORK_FAILURE) {
                throw IOException("Simulation serveur indisponible")
            }
            val now = System.currentTimeMillis()
            fenetreDao.upsertAll(MockData.fenetres.map { it.toEntity(now) })
            _isOfflineMode.value = false
            _errorMessage.value = null
        } catch (e: Exception) {
            _isOfflineMode.value = true
            _errorMessage.value = "Données en cache: source distante indisponible"
        }
    }
}

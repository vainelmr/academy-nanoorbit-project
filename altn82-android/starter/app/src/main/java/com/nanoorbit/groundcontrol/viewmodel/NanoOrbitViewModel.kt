package com.nanoorbit.groundcontrol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nanoorbit.groundcontrol.data.favorites.FavoritesRepository
import com.nanoorbit.groundcontrol.data.local.NanoOrbitDatabase
import com.nanoorbit.groundcontrol.data.models.FenetreCom
import com.nanoorbit.groundcontrol.data.models.Satellite
import com.nanoorbit.groundcontrol.data.repository.NanoOrbitRepository
import com.nanoorbit.groundcontrol.work.PlanningNotificationScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max

/** Désactivé au démarrage : Room + refresh + Worker en parallèle provoquaient fort iowait / ANR. */
private const val AUTO_SCHEDULE_NOTIFICATIONS = false

private const val STARTUP_REFRESH_DELAY_MS = 250L

class NanoOrbitViewModel(application: Application) : AndroidViewModel(application) {
    private lateinit var repository: NanoOrbitRepository

    private val favoritesRepository = FavoritesRepository(application.applicationContext)

    private val _satellites = MutableStateFlow<List<Satellite>>(emptyList())
    val satellites: StateFlow<List<Satellite>> = _satellites.asStateFlow()

    private val _fenetres = MutableStateFlow<List<FenetreCom>>(emptyList())
    val fenetres: StateFlow<List<FenetreCom>> = _fenetres.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isOfflineMode = MutableStateFlow(false)
    val isOfflineMode: StateFlow<Boolean> = _isOfflineMode.asStateFlow()

    private val _cacheAgeLabel = MutableStateFlow<String?>(null)
    val cacheAgeLabel: StateFlow<String?> = _cacheAgeLabel.asStateFlow()

    private val _favoriteSatelliteIds = MutableStateFlow<Set<String>>(emptySet())
    val favoriteSatelliteIds: StateFlow<Set<String>> = _favoriteSatelliteIds.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var planningRescheduleJob: Job? = null

    init {
        viewModelScope.launch {
            val repo = withContext(Dispatchers.IO) {
                val db = NanoOrbitDatabase.getDatabase(getApplication())
                NanoOrbitRepository(db.satelliteDao(), db.fenetreDao())
            }
            repository = repo
            observeLocalCache()

            delay(STARTUP_REFRESH_DELAY_MS)
            runInitialRefresh()
        }
    }

    private fun observeLocalCache() {
        viewModelScope.launch {
            combine(
                repository.observeFenetres(),
                repository.observeSatellites()
            ) { fenetres, satellites ->
                fenetres to satellites
            }
                .distinctUntilChanged()
                .collectLatest { (fenetres, satellites) ->
                    if (_fenetres.value != fenetres) _fenetres.value = fenetres
                    if (_satellites.value != satellites) _satellites.value = satellites

                    if (AUTO_SCHEDULE_NOTIFICATIONS) {
                        schedulePlanningNotificationsDeferred(fenetres, satellites)
                    }
                }
        }
        viewModelScope.launch {
            favoritesRepository.favoriteSatelliteIds.collectLatest { ids ->
                if (_favoriteSatelliteIds.value != ids) _favoriteSatelliteIds.value = ids
            }
        }
        viewModelScope.launch {
            repository.isOfflineMode.collectLatest { v ->
                if (_isOfflineMode.value != v) _isOfflineMode.value = v
            }
        }
        viewModelScope.launch {
            repository.errorMessage.collectLatest { msg ->
                if (_errorMessage.value != msg) _errorMessage.value = msg
            }
        }
        viewModelScope.launch {
            repository.observeLatestCacheTimestamp()
                .distinctUntilChanged()
                .collectLatest { ts ->
                    val label = ts?.let { toCacheAgeLabel(it) }
                    if (_cacheAgeLabel.value != label) _cacheAgeLabel.value = label
                }
        }
    }

    private suspend fun runInitialRefresh() {
        _isLoading.value = true
        try {
            withContext(Dispatchers.IO) {
                repository.refreshSatellites()
                repository.refreshFenetres()
            }
        } finally {
            _isLoading.value = false
        }
    }

    fun loadSatellites() {
        viewModelScope.launch {
            if (!::repository.isInitialized) return@launch
            _isLoading.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.refreshSatellites()
                    repository.refreshFenetres()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshSatellites() {
        loadSatellites()
    }

    fun pullRefreshDashboard() {
        viewModelScope.launch {
            if (!::repository.isInitialized) return@launch
            _isRefreshing.value = true
            try {
                withContext(Dispatchers.IO) {
                    repository.refreshSatellites()
                    repository.refreshFenetres()
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    
    fun scheduleNotificationsForDemo() {
        if (!::repository.isInitialized) return
        viewModelScope.launch(Dispatchers.IO) {
            val fenetres = _fenetres.value
            val satellites = _satellites.value
            runCatching {
                PlanningNotificationScheduler.reschedule(
                    getApplication<Application>().applicationContext,
                    fenetres,
                    satellites.associate { it.idSatellite to it.nomSatellite }
                )
            }
        }
    }

    fun toggleFavoriteSatellite(satelliteId: String) {
        viewModelScope.launch {
            favoritesRepository.toggleFavorite(satelliteId)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun toCacheAgeLabel(cachedAt: Long): String {
        val diffMs = max(0L, System.currentTimeMillis() - cachedAt)
        val minutes = diffMs / 60_000
        return "Mis à jour il y a $minutes min"
    }

    private fun schedulePlanningNotificationsDeferred(
        fenetres: List<FenetreCom>,
        satellites: List<Satellite>,
    ) {
        val ctx = getApplication<Application>().applicationContext
        val namesById = satellites.associate { it.idSatellite to it.nomSatellite }
        planningRescheduleJob?.cancel()
        planningRescheduleJob = viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                PlanningNotificationScheduler.reschedule(ctx, fenetres, namesById)
            }
        }
    }
}

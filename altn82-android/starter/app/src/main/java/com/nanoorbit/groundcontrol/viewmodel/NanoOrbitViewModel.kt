package com.nanoorbit.groundcontrol.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.nanoorbit.groundcontrol.data.local.NanoOrbitDatabase
import com.nanoorbit.groundcontrol.data.models.FenetreCom
import com.nanoorbit.groundcontrol.data.models.Satellite
import com.nanoorbit.groundcontrol.data.repository.NanoOrbitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.max

class NanoOrbitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = NanoOrbitRepository(
        satelliteDao = NanoOrbitDatabase.getDatabase(application).satelliteDao(),
        fenetreDao = NanoOrbitDatabase.getDatabase(application).fenetreDao()
    )

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

    init {
        observeLocalCache()
        loadSatellites()
    }

    private fun observeLocalCache() {
        viewModelScope.launch {
            repository.observeSatellites().collectLatest { _satellites.value = it }
        }
        viewModelScope.launch {
            repository.observeFenetres().collectLatest { _fenetres.value = it }
        }
        viewModelScope.launch {
            repository.isOfflineMode.collectLatest { _isOfflineMode.value = it }
        }
        viewModelScope.launch {
            repository.errorMessage.collectLatest { _errorMessage.value = it }
        }
        viewModelScope.launch {
            repository.observeLatestCacheTimestamp().collectLatest { ts ->
                _cacheAgeLabel.value = ts?.let { toCacheAgeLabel(it) }
            }
        }
    }

    fun loadSatellites() {
        viewModelScope.launch {
            _isLoading.value = true
            repository.refreshSatellites()
            repository.refreshFenetres()
            _isLoading.value = false
        }
    }

    fun refreshSatellites() {
        loadSatellites()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    private fun toCacheAgeLabel(cachedAt: Long): String {
        val diffMs = max(0L, System.currentTimeMillis() - cachedAt)
        val minutes = diffMs / 60_000
        return "Mis à jour il y a $minutes min"
    }
}

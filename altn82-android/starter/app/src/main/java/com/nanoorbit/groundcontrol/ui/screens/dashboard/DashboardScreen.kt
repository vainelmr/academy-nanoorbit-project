package com.nanoorbit.groundcontrol.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.data.models.Satellite
import com.nanoorbit.groundcontrol.data.models.StatutSatellite
import com.nanoorbit.groundcontrol.ui.components.SatelliteCard
import com.nanoorbit.groundcontrol.ui.theme.NanoOrbitTheme
import com.nanoorbit.groundcontrol.viewmodel.NanoOrbitViewModel

@Composable
fun DashboardScreen(
    viewModel: NanoOrbitViewModel = viewModel(),
    onSatelliteClick: (Satellite) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val satellites by viewModel.satellites.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteSatelliteIds.collectAsStateWithLifecycle()
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val cacheAgeLabel by viewModel.cacheAgeLabel.collectAsStateWithLifecycle()
    DashboardContent(
        satellites = satellites,
        searchQuery = searchQuery,
        isLoading = isLoading,
        isRefreshing = isRefreshing,
        favoriteSatelliteIds = favoriteIds,
        isOfflineMode = isOfflineMode,
        cacheAgeLabel = cacheAgeLabel,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSatelliteClick = onSatelliteClick,
        onPullRefresh = viewModel::pullRefreshDashboard,
        onToggleFavorite = viewModel::toggleFavoriteSatellite,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun DashboardContent(
    satellites: List<Satellite>,
    searchQuery: String,
    isLoading: Boolean,
    isRefreshing: Boolean,
    favoriteSatelliteIds: Set<String>,
    isOfflineMode: Boolean,
    cacheAgeLabel: String?,
    onSearchQueryChange: (String) -> Unit,
    onSatelliteClick: (Satellite) -> Unit,
    onPullRefresh: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val orbitesById = remember { MockData.orbites.associateBy { it.idOrbite } }
    var favoritesOnly by remember { mutableStateOf(false) }
    val filteredSatellites = satellites.filter { satellite ->
        val orbiteType = orbitesById[satellite.idOrbite]?.typeOrbite?.name.orEmpty()
        val matchesSearch = satellite.nomSatellite.contains(searchQuery, ignoreCase = true) ||
            orbiteType.contains(searchQuery, ignoreCase = true)
        val matchesFavorite = !favoritesOnly || satellite.idSatellite in favoriteSatelliteIds
        matchesSearch && matchesFavorite
    }
    val operationalCount = satellites.count { it.statut == StatutSatellite.OPERATIONNEL }
    val pullRefreshState =
        rememberPullRefreshState(refreshing = isRefreshing, onRefresh = onPullRefresh)

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Rechercher (nom ou type d'orbite)") },
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = !favoritesOnly,
                            onClick = { favoritesOnly = false },
                            label = { Text("Tous") },
                        )
                        FilterChip(
                            selected = favoritesOnly,
                            onClick = { favoritesOnly = true },
                            label = { Text("Favoris") },
                            enabled = favoriteSatelliteIds.isNotEmpty() || favoritesOnly,
                        )
                    }

                    Text(
                        text = "$operationalCount/${satellites.size} satellites opérationnels",
                        style = MaterialTheme.typography.titleSmall
                    )

                    if (isOfflineMode) {
                        Text(
                            text = "Mode hors-ligne",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge
                        )
                        cacheAgeLabel?.let {
                            Text(text = it, style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (isLoading && satellites.isEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                strokeWidth = 3.dp,
                            )
                            Text(
                                text = "Chargement...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        return@Column
                    }

                    if (filteredSatellites.isEmpty()) {
                        val emptyLabel =
                            if (favoritesOnly) {
                                if (favoriteSatelliteIds.isEmpty()) {
                                    "Aucun favori défini pour le moment"
                                } else {
                                    "Aucun favori correspondant aux critères"
                                }
                            } else {
                                "Aucun satellite trouvé"
                            }
                        Text(
                            text = emptyLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        return@Column
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(
                            items = filteredSatellites,
                            key = { it.idSatellite }
                        ) { satellite ->
                            val orbiteLabel = orbitesById[satellite.idOrbite]?.typeOrbite?.name ?: "N/A"
                            val isFavorite = satellite.idSatellite in favoriteSatelliteIds
                            SatelliteCard(
                                satellite = satellite,
                                orbiteTypeLabel = orbiteLabel,
                                onClick = {
                                    if (satellite.statut != StatutSatellite.DESORBITE) {
                                        onSatelliteClick(satellite)
                                    }
                                },
                                isFavorite = isFavorite,
                                onFavoriteClick = { onToggleFavorite(satellite.idSatellite) },
                            )
                        }
                    }
                }

                PullRefreshIndicator(
                    refreshing = isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    NanoOrbitTheme {
        DashboardContent(
            satellites = MockData.satellites,
            searchQuery = "",
            isLoading = false,
            isRefreshing = false,
            favoriteSatelliteIds = setOf(MockData.satellites.first().idSatellite),
            isOfflineMode = false,
            cacheAgeLabel = "Mis à jour il y a 0 min",
            onSearchQueryChange = {},
            onSatelliteClick = {},
            onPullRefresh = {},
            onToggleFavorite = {}
        )
    }
}

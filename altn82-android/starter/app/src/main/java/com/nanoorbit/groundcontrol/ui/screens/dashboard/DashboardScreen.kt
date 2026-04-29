package com.nanoorbit.groundcontrol.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val isOfflineMode by viewModel.isOfflineMode.collectAsStateWithLifecycle()
    val cacheAgeLabel by viewModel.cacheAgeLabel.collectAsStateWithLifecycle()
    DashboardContent(
        satellites = satellites,
        searchQuery = searchQuery,
        isLoading = isLoading,
        isOfflineMode = isOfflineMode,
        cacheAgeLabel = cacheAgeLabel,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSatelliteClick = onSatelliteClick,
        modifier = modifier
    )
}

@Composable
private fun DashboardContent(
    satellites: List<Satellite>,
    searchQuery: String,
    isLoading: Boolean,
    isOfflineMode: Boolean,
    cacheAgeLabel: String?,
    onSearchQueryChange: (String) -> Unit,
    onSatelliteClick: (Satellite) -> Unit,
    modifier: Modifier = Modifier
) {
    val orbitesById = remember { MockData.orbites.associateBy { it.idOrbite } }
    val filteredSatellites = satellites.filter { satellite ->
        val orbiteType = orbitesById[satellite.idOrbite]?.typeOrbite?.name.orEmpty()
        satellite.nomSatellite.contains(searchQuery, ignoreCase = true) ||
            orbiteType.contains(searchQuery, ignoreCase = true)
    }
    val operationalCount = satellites.count { it.statut == StatutSatellite.OPERATIONNEL }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            color = MaterialTheme.colorScheme.background
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

                if (isLoading) {
                    Text(
                        text = "Chargement...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    return@Column
                }

                if (filteredSatellites.isEmpty()) {
                    Text(
                        text = "Aucun satellite trouvé",
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
                        SatelliteCard(
                            satellite = satellite,
                            orbiteTypeLabel = orbiteLabel,
                            onClick = {
                                if (satellite.statut != StatutSatellite.DESORBITE) {
                                    onSatelliteClick(satellite)
                                }
                            }
                        )
                    }
                }
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
            isOfflineMode = false,
            cacheAgeLabel = "Mis à jour il y a 0 min",
            onSearchQueryChange = {},
            onSatelliteClick = {}
        )
    }
}

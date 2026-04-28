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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.data.models.StatutSatellite
import com.nanoorbit.groundcontrol.ui.components.SatelliteCard
import com.nanoorbit.groundcontrol.ui.theme.NanoOrbitTheme

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    val satellites = remember { MockData.satellites }
    val orbitesById = remember { MockData.orbites.associateBy { it.idOrbite } }
    val filteredSatellites = satellites.filter { satellite ->
        val orbiteType = orbitesById[satellite.idOrbite]?.typeOrbite?.name.orEmpty()
        satellite.nomSatellite.contains(searchQuery, ignoreCase = true) ||
            orbiteType.contains(searchQuery, ignoreCase = true)
    }
    val operationalCount = satellites.count { it.statut == StatutSatellite.OPERATIONNEL }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Rechercher (nom ou type d'orbite)") },
                singleLine = true
            )

            Text(
                text = "$operationalCount/${satellites.size} satellites opérationnels",
                style = MaterialTheme.typography.titleSmall
            )

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
                        orbiteTypeLabel = orbiteLabel
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DashboardScreenPreview() {
    NanoOrbitTheme {
        DashboardScreen()
    }
}

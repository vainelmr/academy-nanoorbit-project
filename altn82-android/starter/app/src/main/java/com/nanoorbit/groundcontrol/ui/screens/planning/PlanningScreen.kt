package com.nanoorbit.groundcontrol.ui.screens.planning

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.data.models.FenetreCom
import com.nanoorbit.groundcontrol.data.models.StatutFenetre
import com.nanoorbit.groundcontrol.data.models.StatutSatellite
import com.nanoorbit.groundcontrol.data.models.Satellite
import com.nanoorbit.groundcontrol.ui.theme.NanoOrbitTheme
import com.nanoorbit.groundcontrol.viewmodel.NanoOrbitViewModel

@Composable
fun PlanningScreen(
    viewModel: NanoOrbitViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    /** Fenêtres : flux Room/cache via le ViewModel (rafraîchi par le repository). */
    val fenetres by viewModel.fenetres.collectAsStateWithLifecycle()
    /** Satellites : même flux que le dashboard pour cohérence nom / statut avec le cache. */
    val satellites by viewModel.satellites.collectAsStateWithLifecycle()
    val satellitesById = remember(satellites) { satellites.associateBy { it.idSatellite } }
    PlanningScreenBody(
        fenetresRoom = fenetres,
        satellitesById = satellitesById,
        modifier = modifier
    )
}

@Composable
private fun PlanningScreenBody(
    fenetresRoom: List<FenetreCom>,
    satellitesById: Map<String, Satellite>,
    modifier: Modifier = Modifier
) {
    val stations = remember { MockData.stations }
    val stationsByCode = remember { stations.associateBy { it.codeStation } }
    val allFenetres = remember(fenetresRoom) {
        fenetresRoom.sortedBy { it.datetimeDebut }
    }

    var selectedStationCode by remember { mutableStateOf<String?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }

    val displayedFenetres = allFenetres.filter { fenetre ->
        selectedStationCode == null || fenetre.codeStation == selectedStationCode
    }
    val totalDuree = displayedFenetres.sumOf { it.duree }
    val totalVolume = displayedFenetres.sumOf { it.volumeDonnees ?: 0.0 }
    val hasVolume = displayedFenetres.any { it.volumeDonnees != null }

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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Planning des communications",
                    style = MaterialTheme.typography.titleLarge
                )

                Column {
                    OutlinedButton(onClick = { menuExpanded = true }) {
                        val selectedLabel = selectedStationCode?.let { code ->
                            stationsByCode[code]?.nomStation ?: code
                        } ?: "Toutes"
                        Text("Station: $selectedLabel")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Toutes") },
                            onClick = {
                                selectedStationCode = null
                                menuExpanded = false
                            }
                        )
                        stations.forEach { station ->
                            DropdownMenuItem(
                                text = { Text(station.nomStation) },
                                onClick = {
                                    selectedStationCode = station.codeStation
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Durée totale de contact : $totalDuree s")
                        Text(
                            if (hasVolume) {
                                "Volume total planifié : ${"%.1f".format(totalVolume)}"
                            } else {
                                "Volume total planifié : non disponible"
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayedFenetres, key = { it.idFenetre }) { fenetre ->
                        val satellite = satellitesById[fenetre.idSatellite]
                        val station = stationsByCode[fenetre.codeStation]
                        FenetreItemCard(
                            fenetre = fenetre,
                            satelliteName = satellite?.nomSatellite ?: fenetre.idSatellite,
                            satelliteStatut = satellite?.statut,
                            stationName = station?.nomStation ?: fenetre.codeStation
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FenetreItemCard(
    fenetre: FenetreCom,
    satelliteName: String,
    satelliteStatut: StatutSatellite?,
    stationName: String
) {
    val statusColor = when (fenetre.statut) {
        StatutFenetre.PLANIFIEE -> MaterialTheme.colorScheme.primary
        StatutFenetre.REALISEE -> Color(0xFF2E7D32)
        StatutFenetre.ANNULEE -> MaterialTheme.colorScheme.error
    }

    val errors = remember(fenetre.duree, satelliteStatut) {
        val list = mutableListOf<String>()
        // Mirror Android-side validation of Oracle business rules RG-F04 / RG-S06.
        if (fenetre.duree !in 1..900) {
            list.add("Durée invalide : doit être comprise entre 1 et 900 secondes")
        }
        if (satelliteStatut == StatutSatellite.DESORBITE) {
            list.add("Satellite désorbité : nouvelle communication interdite")
        }
        list
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = fenetre.statut.name, color = statusColor, style = MaterialTheme.typography.labelLarge)
                Text(text = fenetre.datetimeDebut.toString(), style = MaterialTheme.typography.bodySmall)
            }
            Text("Satellite : $satelliteName")
            Text("Station : $stationName")
            Text("Durée : ${fenetre.duree} s")
            Text("Volume : ${fenetre.volumeDonnees?.toString() ?: "N/A"}")

            errors.forEach { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlanningScreenPreview() {
    NanoOrbitTheme {
        PlanningScreenBody(
            fenetresRoom = MockData.fenetres,
            satellitesById = MockData.satellites.associateBy { it.idSatellite }
        )
    }
}

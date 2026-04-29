package com.nanoorbit.groundcontrol.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.ui.components.StatusBadge
import com.nanoorbit.groundcontrol.viewmodel.NanoOrbitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    satelliteId: String,
    onBack: () -> Unit,
    viewModel: NanoOrbitViewModel = viewModel()
) {
    val satellites by viewModel.satellites.collectAsStateWithLifecycle()
    val satellite = satellites.find { it.idSatellite == satelliteId }
    val orbite = satellite?.let { sat -> MockData.orbites.find { it.idOrbite == sat.idOrbite } }

    var showDialog by remember { mutableStateOf(false) }
    var anomalyMessage by remember { mutableStateOf("") }
    var anomalyError by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                anomalyError = false
            },
            title = { Text("Signaler une anomalie") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = anomalyMessage,
                        onValueChange = {
                            anomalyMessage = it
                            if (anomalyError && it.isNotBlank()) anomalyError = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Message") },
                        isError = anomalyError,
                        singleLine = false
                    )
                    if (anomalyError) {
                        Text(
                            text = "Le message ne peut pas être vide.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (anomalyMessage.isBlank()) {
                            anomalyError = true
                        } else {
                            showDialog = false
                            anomalyError = false
                            anomalyMessage = ""
                        }
                    }
                ) {
                    Text("Envoyer")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        anomalyError = false
                    }
                ) {
                    Text("Annuler")
                }
            }
        )
    }

    if (satellite == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Détail satellite") },
                    navigationIcon = {
                        TextButton(onClick = onBack) {
                            Text("Retour")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Satellite introuvable",
                    style = MaterialTheme.typography.titleMedium
                )
                Button(onClick = onBack) {
                    Text("Retour")
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(satellite.nomSatellite) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Retour")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Statut", style = MaterialTheme.typography.titleSmall)
                        StatusBadge(statut = satellite.statut)
                        Text("Format CubeSat : ${satellite.formatCubesat}")
                        Text("Type d'orbite : ${orbite?.typeOrbite ?: "N/A"}")
                        Text("Altitude orbitale : ${orbite?.altitude?.toString() ?: "N/A"} km")
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Télémétrie", style = MaterialTheme.typography.titleSmall)
                        Text("Masse : ${satellite.masse?.toString() ?: "N/A"} kg")
                        Text("Date de lancement : ${satellite.dateLancement?.toString() ?: "N/A"}")
                        Text("Durée de vie estimée : donnée non disponible")
                        Text("Batterie : donnée non disponible")
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Instruments embarqués", style = MaterialTheme.typography.titleSmall)
                        Text("Aucun instrument embarqué disponible")
                    }
                }
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Missions", style = MaterialTheme.typography.titleSmall)
                        Text("Aucune mission active disponible")
                    }
                }
            }

            item {
                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Signaler une anomalie")
                }
            }
        }
    }
}

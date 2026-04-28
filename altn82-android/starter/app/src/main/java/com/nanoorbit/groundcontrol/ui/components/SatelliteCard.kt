package com.nanoorbit.groundcontrol.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nanoorbit.groundcontrol.data.mock.MockData
import com.nanoorbit.groundcontrol.data.models.Satellite
import com.nanoorbit.groundcontrol.data.models.StatutSatellite
import com.nanoorbit.groundcontrol.ui.theme.NanoOrbitTheme

@Composable
fun SatelliteCard(
    satellite: Satellite,
    orbiteTypeLabel: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val isDesorbite = satellite.statut == StatutSatellite.DESORBITE

    val cardModifier = if (!isDesorbite && onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .alpha(if (isDesorbite) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = satellite.nomSatellite,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Format: ${satellite.formatCubesat}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(text = "Orbite: $orbiteTypeLabel", style = MaterialTheme.typography.bodyMedium)
            StatusBadge(statut = satellite.statut)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SatelliteCardPreview() {
    NanoOrbitTheme {
        SatelliteCard(
            satellite = MockData.satellites.first(),
            orbiteTypeLabel = "SSO"
        )
    }
}

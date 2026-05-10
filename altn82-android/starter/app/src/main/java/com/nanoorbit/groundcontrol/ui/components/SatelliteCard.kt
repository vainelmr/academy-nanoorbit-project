package com.nanoorbit.groundcontrol.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    onClick: (() -> Unit)? = null,
    isFavorite: Boolean = false,
    onFavoriteClick: (() -> Unit)? = null
) {
    val isDesorbite = satellite.statut == StatutSatellite.DESORBITE

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (isDesorbite) 0.6f else 1f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 8.dp)
                    .then(
                        if (!isDesorbite && onClick != null) Modifier.clickable { onClick() }
                        else Modifier
                    ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
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
            if (onFavoriteClick != null) {
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris"
                    )
                }
            }
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

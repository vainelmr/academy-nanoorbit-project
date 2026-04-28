package com.nanoorbit.groundcontrol.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nanoorbit.groundcontrol.data.models.StatutSatellite
import com.nanoorbit.groundcontrol.ui.theme.NanoOrbitTheme

@Composable
fun StatusBadge(
    statut: StatutSatellite,
    modifier: Modifier = Modifier
) {
    val color = when (statut) {
        StatutSatellite.OPERATIONNEL -> Color(0xFF2E7D32)
        StatutSatellite.EN_VEILLE -> Color(0xFFEF6C00)
        StatutSatellite.DEFAILLANT -> Color(0xFFC62828)
        StatutSatellite.DESORBITE -> Color(0xFF757575)
    }

    Text(
        text = statut.name,
        color = Color.White,
        style = MaterialTheme.typography.labelMedium,
        modifier = modifier
            .background(color = color, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun StatusBadgePreview() {
    NanoOrbitTheme {
        StatusBadge(statut = StatutSatellite.OPERATIONNEL)
    }
}

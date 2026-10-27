package com.alanduran.weatherreport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alanduran.weatherreport.data.model.AirQuality
import com.alanduran.weatherreport.ui.WeatherUiState
import com.alanduran.weatherreport.ui.theme.GlassCard
import com.alanduran.weatherreport.ui.theme.TextPrimary
import com.alanduran.weatherreport.ui.theme.TextSecondary
import com.alanduran.weatherreport.ui.theme.TextTertiary
import com.alanduran.weatherreport.ui.theme.VioletBright

@Composable
fun AirQualityScreen(
    state: WeatherUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val airQuality = state.data?.airQuality
    Column(modifier.fillMaxSize()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextSecondary
                )
            }
            Text(
                text = "Calidad del aire",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        if (airQuality == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Sin datos de calidad del aire disponibles.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { AqiHeroCard(airQuality) }
                item { PollutantCard("PM2.5", "Partículas finas", airQuality.pm25) }
                item { PollutantCard("PM10", "Partículas gruesas", airQuality.pm10) }
                item { PollutantCard("O\u2083", "Ozono", airQuality.ozone) }
                item { PollutantCard("NO\u2082", "Dióxido de nitrógeno", airQuality.no2) }
                item { PollutantCard("SO\u2082", "Dióxido de azufre", airQuality.so2) }
                item { PollutantCard("CO", "Monóxido de carbono", airQuality.co) }
                item { AqiScaleCard(airQuality.aqi) }
            }
        }
    }
}

@Composable
private fun AqiHeroCard(airQuality: AirQuality) {
    val color = Color(airQuality.categoryColor)
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 22.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Índice de Calidad del Aire",
                style = MaterialTheme.typography.labelLarge,
                color = TextTertiary
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = airQuality.aqi?.toString() ?: "--",
                        style = MaterialTheme.typography.displayLarge,
                        color = color
                    )
                    Text(
                        text = "ICA",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondary
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = airQuality.category,
                style = MaterialTheme.typography.titleMedium,
                color = color
            )
        }
    }
}

@Composable
private fun PollutantCard(name: String, description: String, value: Double?) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                modifier = Modifier.width(56.dp)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value?.let { "%.1f \u00B5g/m\u00B3".format(it) } ?: "--",
                style = MaterialTheme.typography.titleMedium,
                color = VioletBright
            )
        }
    }
}

@Composable
private fun AqiScaleCard(current: Int?) {
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Escala del índice",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(10.dp))
        val ranges = listOf(
            Triple("0 - 50", "Bueno", 0xFF43E08B),
            Triple("51 - 100", "Moderado", 0xFFFFB33D),
            Triple("101 - 150", "Dañino para grupos sensibles", 0xFFFF8A3D),
            Triple("151 - 200", "Dañino", 0xFFFF5252),
            Triple("201 - 300", "Muy dañino", 0xFFB44DD8),
            Triple("301+", "Peligroso", 0xFF8B1A4B)
        )
        ranges.forEach { (range, label, color) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(color))
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = range,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.width(80.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (isInRange(current, range)) {
                    Text(
                        text = "Actual",
                        style = MaterialTheme.typography.labelSmall,
                        color = VioletBright
                    )
                }
            }
        }
    }
}

private fun isInRange(current: Int?, range: String): Boolean {
    if (current == null) return false
    return when (range) {
        "0 - 50" -> current <= 50
        "51 - 100" -> current in 51..100
        "101 - 150" -> current in 101..150
        "151 - 200" -> current in 151..200
        "201 - 300" -> current in 201..300
        else -> current > 300
    }
}

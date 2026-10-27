package com.alanduran.weatherreport.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alanduran.weatherreport.data.model.DailyPoint
import com.alanduran.weatherreport.data.model.HourlyPoint
import com.alanduran.weatherreport.data.model.WeatherData
import com.alanduran.weatherreport.data.model.WeatherSource
import com.alanduran.weatherreport.ui.WeatherUiState
import com.alanduran.weatherreport.ui.theme.GlassCard
import com.alanduran.weatherreport.ui.components.HourlyStrip
import com.alanduran.weatherreport.ui.components.HumidityRing
import com.alanduran.weatherreport.ui.components.PressureGauge
import com.alanduran.weatherreport.ui.components.SunArc
import com.alanduran.weatherreport.ui.components.TempRangeBar
import com.alanduran.weatherreport.ui.components.TemperatureLineChart
import com.alanduran.weatherreport.ui.components.UvArcGauge
import com.alanduran.weatherreport.ui.components.WindCompass
import com.alanduran.weatherreport.ui.theme.TextPrimary
import com.alanduran.weatherreport.ui.theme.TextSecondary
import com.alanduran.weatherreport.ui.theme.TextTertiary
import com.alanduran.weatherreport.ui.theme.VioletBright
import com.alanduran.weatherreport.util.aqiCategory
import com.alanduran.weatherreport.util.formatClock
import com.alanduran.weatherreport.util.formatTemperature
import com.alanduran.weatherreport.util.formatWindSpeed
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    state: WeatherUiState,
    onRefresh: () -> Unit,
    onOpenAirQuality: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val data = state.data
    Box(modifier.fillMaxSize()) {
        if (data == null) {
            LoadingContent(state.error, onRefresh)
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item { TopHeader(data.location, state.refreshing, onRefresh, onOpenSettings) }
                item { HeroCard(data) }
                item { AirQualityQuickCard(data.current.aqi, onOpenAirQuality) }
                item { ForecastCard(data.daily) }
                item { HourlyCard(data.hourly) }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricCard("Índice UV", Modifier.weight(1f)) {
                            UvArcGauge(
                                index = data.uv.index,
                                category = data.uv.category,
                                modifier = Modifier.fillMaxWidth().height(96.dp)
                            )
                        }
                        MetricCard("Humedad relativa", Modifier.weight(1f)) {
                            HumidityRing(data.current.humidity, Modifier.size(96.dp))
                        }
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricCard("Sensación térmica", Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = formatTemperature(data.current.feelsLike),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Sensación real",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary
                                )
                            }
                        }
                        MetricCard("Viento", Modifier.weight(1f)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                WindCompass(
                                    direction = data.current.windDirection,
                                    modifier = Modifier.size(72.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "${data.current.windDirectionLabel} · " +
                                        formatWindSpeed(data.current.windSpeed),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        MetricCard("Amanecer y atardecer", Modifier.weight(1f)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                SunArc(
                                    sunrise = data.sun.sunrise,
                                    sunset = data.sun.sunset,
                                    modifier = Modifier.fillMaxWidth().height(58.dp)
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "\u2600 ${formatClock(data.sun.sunrise)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                    Text(
                                        text = "${formatClock(data.sun.sunset)} \u263E",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                        MetricCard("Presión atmosférica", Modifier.weight(1f)) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "${data.current.pressure.roundToInt()} mbar",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimary
                                )
                                Spacer(Modifier.height(12.dp))
                                PressureGauge(
                                    pressure = data.current.pressure,
                                    modifier = Modifier.fillMaxWidth().height(20.dp)
                                )
                            }
                        }
                    }
                }
                item { SourceFooter(data) }
            }
        }

        if (state.error != null && data != null) {
            ErrorBanner(
                message = state.error,
                onRetry = onRefresh,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
private fun TopHeader(
    location: String,
    refreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = VioletBright,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = location,
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (refreshing) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = VioletBright
            )
            Spacer(Modifier.width(8.dp))
        }
        IconButton(onClick = onRefresh) {
            Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = TextSecondary)
        }
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Default.Settings, contentDescription = "Ajustes", tint = TextSecondary)
        }
    }
}

@Composable
private fun HeroCard(data: WeatherData) {
    GlassCard(modifier = Modifier.fillMaxWidth(), contentPadding = 22.dp) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = data.current.conditionEmoji, fontSize = 36.sp)
            Text(
                text = formatTemperature(data.current.temperature),
                style = MaterialTheme.typography.displayLarge,
                color = TextPrimary
            )
            Text(
                text = data.current.conditionLabel,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Máx ${formatTemperature(data.current.tempMax)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Text(text = "·", color = TextTertiary)
                Text(
                    text = "Mín ${formatTemperature(data.current.tempMin)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun AirQualityQuickCard(aqi: Int?, onClick: () -> Unit) {
    val category = aqiCategory(aqi)
    val categoryColor = Color(category.color)
    GlassCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = aqi?.toString() ?: "--",
                    style = MaterialTheme.typography.titleMedium,
                    color = categoryColor
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Calidad del aire (ICA)",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextTertiary
                )
                Text(
                    text = category.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
            }
            Text(
                text = "Informe",
                style = MaterialTheme.typography.labelLarge,
                color = VioletBright
            )
        }
    }
}

@Composable
private fun ForecastCard(daily: List<DailyPoint>) {
    if (daily.isEmpty()) return
    var expanded by remember { mutableStateOf(false) }
    val visible = if (expanded) daily else daily.take(5)
    val globalMin = daily.minOf { it.tempMin }
    val globalMax = daily.maxOf { it.tempMax }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pronóstico de 5 días",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(10.dp))
        visible.forEach { day ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
            ) {
                Text(
                    text = day.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.width(66.dp)
                )
                Text(text = day.conditionEmoji, fontSize = 18.sp, modifier = Modifier.width(28.dp))
                Text(
                    text = formatTemperature(day.tempMin),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(38.dp)
                )
                TempRangeBar(
                    min = day.tempMin,
                    max = day.tempMax,
                    globalMin = globalMin,
                    globalMax = globalMax,
                    modifier = Modifier
                        .weight(1f)
                        .height(18.dp)
                        .padding(horizontal = 8.dp)
                )
                Text(
                    text = formatTemperature(day.tempMax),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.width(38.dp)
                )
            }
        }
        if (daily.size > 5) {
            TextButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(
                    text = if (expanded) "Ver menos" else "Ver más días",
                    color = VioletBright
                )
            }
        }
    }
}

@Composable
private fun HourlyCard(hourly: List<HourlyPoint>) {
    if (hourly.isEmpty()) return
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Pronóstico de 24 horas",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary
        )
        Spacer(Modifier.height(10.dp))
        TemperatureLineChart(
            points = hourly,
            modifier = Modifier.fillMaxWidth().height(130.dp)
        )
        Spacer(Modifier.height(14.dp))
        HourlyStrip(points = hourly, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun MetricCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GlassCard(modifier = modifier, contentPadding = 14.dp) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = TextTertiary
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun SourceFooter(data: WeatherData) {
    val sourceText = when (data.source) {
        WeatherSource.STATION -> "Estación meteorológica"
        WeatherSource.OPEN_METEO -> "Open-Meteo"
        WeatherSource.MIXED -> "Estación + Open-Meteo"
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
    ) {
        data.stationName?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
        Text(
            text = "Fuente: $sourceText",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
        data.stationUpdatedAt?.takeIf { it.isNotBlank() }?.let {
            Text(
                text = "Último registro: $it",
                style = MaterialTheme.typography.labelSmall,
                color = TextTertiary
            )
        }
        Text(
            text = "Actualizado ${formatClock(data.updatedAt)}",
            style = MaterialTheme.typography.labelSmall,
            color = TextTertiary
        )
    }
}

@Composable
private fun LoadingContent(error: String?, onRetry: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(32.dp)
    ) {
        CircularProgressIndicator(color = VioletBright)
        Spacer(Modifier.height(18.dp))
        Text(
            text = error ?: "Consultando el clima...",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        if (error != null) {
            Spacer(Modifier.height(18.dp))
            Button(onClick = onRetry) { Text("Reintentar") }
        }
    }
}

@Composable
private fun ErrorBanner(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = onRetry) {
                Text("Reintentar", color = VioletBright)
            }
        }
    }
}

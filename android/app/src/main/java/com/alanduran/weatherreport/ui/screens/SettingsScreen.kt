package com.alanduran.weatherreport.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import com.alanduran.weatherreport.data.AppSettings
import com.alanduran.weatherreport.ui.theme.GlassCard
import com.alanduran.weatherreport.ui.theme.TextPrimary
import com.alanduran.weatherreport.ui.theme.TextSecondary
import com.alanduran.weatherreport.ui.theme.TextTertiary
import com.alanduran.weatherreport.ui.theme.VioletBright

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSave: (AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var host by remember(settings) { mutableStateOf(settings.backendHost) }
    var useStation by remember(settings) { mutableStateOf(settings.useStation) }
    var latitude by remember(settings) { mutableStateOf(settings.latitude.toString()) }
    var longitude by remember(settings) { mutableStateOf(settings.longitude.toString()) }
    var locationName by remember(settings) { mutableStateOf(settings.locationName) }
    var stationUser by remember(settings) { mutableStateOf(settings.stationUser) }
    var stationPassword by remember(settings) { mutableStateOf(settings.stationPassword) }
    var refreshMinutes by remember(settings) { mutableStateOf(settings.refreshMinutes.toString()) }

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
                text = "Ajustes",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 18.dp, end = 18.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Estación meteorológica",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Conéctate al backend WR_web (Spring Boot) en tu red local. " +
                        "Si no está disponible, la app usa Open-Meteo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Servidor (IP:puerto)") },
                    placeholder = { Text("10.0.2.2:8080") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stationUser,
                    onValueChange = { stationUser = it },
                    label = { Text("Usuario") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = stationPassword,
                    onValueChange = { stationPassword = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Usar datos de la estación",
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )
                        Text(
                            text = "Mezcla lecturas reales con el pronóstico",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary
                        )
                    }
                    Switch(checked = useStation, onCheckedChange = { useStation = it })
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = locationName,
                    onValueChange = { locationName = it },
                    label = { Text("Nombre del lugar") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitud") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitud") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Actualización",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = refreshMinutes,
                    onValueChange = { refreshMinutes = it },
                    label = { Text("Intervalo (minutos)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = {
                    onSave(
                        AppSettings(
                            backendHost = host.trim(),
                            useStation = useStation,
                            latitude = latitude.toDoubleOrNull() ?: settings.latitude,
                            longitude = longitude.toDoubleOrNull() ?: settings.longitude,
                            locationName = locationName.trim().ifBlank { settings.locationName },
                            stationUser = stationUser.trim(),
                            stationPassword = stationPassword,
                            refreshMinutes = refreshMinutes.toIntOrNull() ?: settings.refreshMinutes
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar y actualizar")
            }

            Text(
                text = "Consejo: en el emulador usa 10.0.2.2:8080 para llegar a tu PC. " +
                    "En un dispositivo físico usa la IP de tu PC (ej. 192.168.1.20:8080).",
                style = MaterialTheme.typography.labelSmall,
                color = VioletBright
            )
        }
    }
}

package com.alanduran.weatherreport.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.alanduran.weatherreport.data.AppSettings
import com.alanduran.weatherreport.data.SettingsStore
import com.alanduran.weatherreport.data.model.WeatherData
import com.alanduran.weatherreport.data.repository.WeatherRepository

data class WeatherUiState(
    val loading: Boolean = true,
    val refreshing: Boolean = false,
    val error: String? = null,
    val data: WeatherData? = null
)

class WeatherViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsStore = SettingsStore(app)
    private val repository = WeatherRepository(settingsStore)

    private val _state = MutableStateFlow(WeatherUiState())
    val state: StateFlow<WeatherUiState> = _state.asStateFlow()

    val settings: StateFlow<AppSettings> = settingsStore.data
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private var loadJob: Job? = null

    init {
        refresh()
        viewModelScope.launch {
            settings.collectLatest { current ->
                while (true) {
                    delay(current.refreshMinutes.coerceAtLeast(1) * 60_000L)
                    refresh()
                }
            }
        }
    }

    fun refresh() {
        if (loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            val hasData = _state.value.data != null
            _state.value = _state.value.copy(
                loading = !hasData,
                refreshing = hasData,
                error = null
            )
            runCatching { repository.load() }
                .onSuccess { data ->
                    _state.value = WeatherUiState(
                        loading = false,
                        refreshing = false,
                        error = null,
                        data = data
                    )
                }
                .onFailure { error ->
                    _state.value = _state.value.copy(
                        loading = false,
                        refreshing = false,
                        error = error.message ?: "No se pudo actualizar el clima"
                    )
                }
        }
    }

    fun saveSettings(newSettings: AppSettings) {
        viewModelScope.launch {
            settingsStore.update(newSettings)
            loadJob?.cancel()
            loadJob = null
            refresh()
        }
    }
}

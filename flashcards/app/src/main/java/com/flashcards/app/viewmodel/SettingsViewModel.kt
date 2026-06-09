package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.domain.AppSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: ShineRepository,
) : ViewModel() {
    val uiState: StateFlow<AppSettings> = repository.observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setThemeMode(mode: String) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setDefaultAlgorithm(algorithm: String) {
        viewModelScope.launch { repository.setDefaultAlgorithm(algorithm) }
    }

    fun setDefaultPreset(preset: String) {
        viewModelScope.launch { repository.setDefaultPreset(preset) }
    }

    fun setDefaultSmartSchedule(enabled: Boolean) {
        viewModelScope.launch { repository.setDefaultSmartSchedule(enabled) }
    }

    fun setMaxSessionsPerDay(max: Int) {
        viewModelScope.launch { repository.setGlobalMaxSessionsPerDay(max) }
    }
}

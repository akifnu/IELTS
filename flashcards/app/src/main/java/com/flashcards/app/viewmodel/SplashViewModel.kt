package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.data.auth.AuthRepository
import com.flashcards.app.domain.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplashUiState(
    val session: UserSession = UserSession(),
    val onboarded: Boolean = false,
    val ready: Boolean = false,
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: ShineRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val session = authRepository.session.first()
            _uiState.update {
                it.copy(session = session, onboarded = settings.onboarded, ready = true)
            }
        }
    }

    fun continueAsGuest(name: String = "") {
        viewModelScope.launch { authRepository.continueAsGuest(name) }
    }
}

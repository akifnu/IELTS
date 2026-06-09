package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.data.auth.AuthRepository
import com.flashcards.app.data.auth.SessionStore
import com.flashcards.app.domain.SplashScenes
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
    val userName: String = "",
    val ready: Boolean = false,
    val sceneIndex: Int = 0,
    val animateContent: Boolean = false,
)

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val repository: ShineRepository,
    private val authRepository: AuthRepository,
    private val sessionStore: SessionStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    val currentScene get() = SplashScenes.all[_uiState.value.sceneIndex]

    init {
        viewModelScope.launch {
            val settings = repository.getSettings()
            val session = authRepository.session.first()
            val saved = sessionStore.getLastSplashScene()
            val startIndex = if (saved != null) {
                SplashScenes.pickRandom(saved)
            } else {
                SplashScenes.pickRandom(-1)
            }
            _uiState.update {
                it.copy(
                    session = session,
                    onboarded = settings.onboarded,
                    userName = settings.userName,
                    ready = true,
                    sceneIndex = startIndex,
                    animateContent = true,
                )
            }
            sessionStore.setLastSplashScene(startIndex)
        }
    }

    fun personalLine(): String {
        val name = _uiState.value.userName.trim()
        return if (name.isNotBlank()) "$name, this moment is yours." else "This moment is yours."
    }

    fun cycleScene() {
        viewModelScope.launch {
            val current = _uiState.value.sceneIndex
            val next = SplashScenes.pickRandom(current)
            _uiState.update { it.copy(animateContent = false, sceneIndex = next) }
            sessionStore.setLastSplashScene(next)
            _uiState.update { it.copy(animateContent = true) }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch { authRepository.continueAsGuest(_uiState.value.userName) }
    }

    fun signInGoogle(id: String, name: String?, avatar: String?, onDone: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                authRepository.signInWithGoogle(id, null, name, avatar)
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Google sign-in failed")
            }
        }
    }
}

package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import com.flashcards.app.data.auth.AuthRepository
import com.flashcards.app.domain.AppSettings
import com.flashcards.app.domain.Deck
import com.flashcards.app.domain.InboxItem
import com.flashcards.app.domain.UserSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountUiState(
    val settings: AppSettings = AppSettings(),
    val session: UserSession = UserSession(),
    val deckCount: Int = 0,
    val clusterCount: Int = 0,
    val inbox: List<InboxItem> = emptyList(),
    val decks: List<Deck> = emptyList(),
    val authError: String? = null,
    val authBusy: Boolean = false,
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val repository: ShineRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {
    private val _authState = MutableStateFlow(Pair<String?, Boolean>(null, false))

    val uiState: StateFlow<AccountUiState> = combine(
        combine(
            repository.observeSettings(),
            repository.observeDecks(),
            repository.observeClusters(),
            repository.observeInbox(),
            authRepository.session,
        ) { settings, decks, clusters, inbox, session ->
            AccountUiState(
                settings = settings,
                session = session,
                deckCount = decks.size,
                clusterCount = clusters.size,
                inbox = inbox,
                decks = decks,
            )
        },
        _authState,
    ) { state, auth ->
        state.copy(authError = auth.first, authBusy = auth.second)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AccountUiState())

    fun updateUserName(name: String) {
        viewModelScope.launch { repository.updateUserName(name) }
    }

    suspend fun exportBackup(): String = repository.exportBackup()

    fun importBackup(json: String, onDone: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.importBackup(json)
                onDone()
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun acceptInbox(item: InboxItem, clusterId: Long?, onDone: (Long) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val deckId = repository.acceptSharedDeck(item.payloadJson, clusterId)
                repository.dismissInbox(item.id)
                onDone(deckId)
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun dismissInbox(id: String) {
        viewModelScope.launch { repository.dismissInbox(id) }
    }

    fun importShare(json: String, clusterId: Long?, onDone: (Long) -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                val deckId = repository.acceptSharedDeck(json, clusterId)
                onDone(deckId)
            } catch (_: Exception) {
                onError()
            }
        }
    }

    fun registerEmail(name: String, email: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _authState.update { null to true }
            try {
                authRepository.registerWithEmail(name, email, password)
                _authState.update { null to false }
                onDone()
            } catch (e: Exception) {
                _authState.update { (e.message ?: "Registration failed") to false }
            }
        }
    }

    fun signInEmail(email: String, password: String, onDone: () -> Unit) {
        viewModelScope.launch {
            _authState.update { null to true }
            try {
                authRepository.signInWithEmail(email, password)
                _authState.update { null to false }
                onDone()
            } catch (e: Exception) {
                _authState.update { (e.message ?: "Sign in failed") to false }
            }
        }
    }

    fun signInGoogle(id: String, email: String?, name: String?, avatar: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            _authState.update { null to true }
            try {
                authRepository.signInWithGoogle(id, email, name, avatar)
                _authState.update { null to false }
                onDone()
            } catch (e: Exception) {
                _authState.update { (e.message ?: "Google sign in failed") to false }
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            authRepository.signOut()
            onDone()
        }
    }

    fun clearAuthError() {
        _authState.update { null to false }
    }
}

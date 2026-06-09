package com.flashcards.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flashcards.app.data.ShineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val repository: ShineRepository,
) : ViewModel() {

    fun loadSampleDecks(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.seedSampleData()
            onDone()
        }
    }

    fun startEmpty(onDone: () -> Unit) {
        viewModelScope.launch {
            repository.markOnboarded()
            onDone()
        }
    }
}

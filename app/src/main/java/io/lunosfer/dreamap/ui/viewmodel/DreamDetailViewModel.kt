package io.lunosfer.dreamap.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.lunosfer.dreamap.data.model.DreamDetail
import io.lunosfer.dreamap.data.repository.DreamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DreamDetailUiState {
    object Loading : DreamDetailUiState()
    data class Success(val dream: DreamDetail) : DreamDetailUiState()
    data class Error(val message: String) : DreamDetailUiState()
}

class DreamDetailViewModel : ViewModel() {
    private val repository = DreamRepository()
    private val _state = MutableStateFlow<DreamDetailUiState>(DreamDetailUiState.Loading)
    val state: StateFlow<DreamDetailUiState> = _state.asStateFlow()

    fun loadDream(id: Long) {
        _state.value = DreamDetailUiState.Loading
        viewModelScope.launch {
            repository.getDream(id).onSuccess { dream ->
                _state.value = DreamDetailUiState.Success(dream)
            }.onFailure { error ->
                _state.value = DreamDetailUiState.Error(error.message ?: "Unknown error")
            }
        }
    }

    fun analyzeDream(id: Long, content: String, lang: String) {
        viewModelScope.launch {
            // Optimistic loading or just wait for success then reload
            repository.analyzeDream(id, content, lang)
            // Reload dream to get the new status
            loadDream(id)
        }
    }
}

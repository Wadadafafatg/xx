package com.example.freshstart.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freshstart.data.Resource
import com.example.freshstart.domain.repository.SampleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SampleViewModel(
    private val repository: SampleRepository
) : ViewModel() {

    private val _uiState: MutableStateFlow<Resource<String>> = MutableStateFlow(Resource.Loading)
    val uiState: StateFlow<Resource<String>> = _uiState.asStateFlow()

    init {
        loadSampleData()
    }

    fun loadSampleData() {
        viewModelScope.launch {
            _uiState.value = Resource.Loading
            try {
                val message = repository.fetchSampleMessage()
                _uiState.value = Resource.Success(message)
            } catch (throwable: Throwable) {
                _uiState.value = Resource.Error(
                    message = throwable.message ?: "Unexpected error",
                    throwable = throwable
                )
            }
        }
    }
}

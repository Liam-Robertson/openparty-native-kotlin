package com.openparty.app.features.startup.feature_splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.core.shared.presentation.UiState
import com.openparty.app.features.startup.feature_authentication.domain.usecase.DetermineAuthStatesUseCase
import com.openparty.app.features.startup.feature_authentication.presentation.AuthFlowNavigationMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val determineAuthStatesUseCase: DetermineAuthStatesUseCase,
    private val authFlowNavigationMapper: AuthFlowNavigationMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    init {
        viewModelScope.launch {
            navigateToNextAuthScreen()
        }
    }

    private suspend fun navigateToNextAuthScreen() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        when (val authStatesResult = determineAuthStatesUseCase()) {
            is DomainResult.Success -> {
                val destination = authFlowNavigationMapper.determineDestination(authStatesResult.data)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = null)
                _uiEvent.emit(UiEvent.Navigate(destination))
            }
            is DomainResult.Failure -> {
                val errorMessage = AppErrorMapper.getUserFriendlyMessage(authStatesResult.error)
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
            }
        }
    }
}

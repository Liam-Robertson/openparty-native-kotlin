package com.openparty.app.features.startup.feature_screen_name_generation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.features.shared.feature_user.domain.model.UpdateUserRequest
import com.openparty.app.features.shared.feature_user.domain.usecase.UpdateUserUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.DetermineAuthStatesUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.GetCurrentUserIdUseCase
import com.openparty.app.features.startup.feature_authentication.presentation.AuthFlowNavigationMapper
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.startup.account.shared.presentation.model.AccountUiStateUpdate
import com.openparty.app.features.startup.feature_screen_name_generation.domain.usecase.GenerateUniqueScreenNameUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreenNameGenerationViewModel @Inject constructor(
    private val generateUniqueScreenNameUseCase: GenerateUniqueScreenNameUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val determineAuthStatesUseCase: DetermineAuthStatesUseCase,
    private val authFlowNavigationMapper: AuthFlowNavigationMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScreenNameGenerationUiState())
    val uiState: StateFlow<ScreenNameGenerationUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    init {
        refreshName()
    }

    fun refreshName() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = generateUniqueScreenNameUseCase()) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        currentName = result.data,
                        isLoading = false,
                        errorMessage = null
                    )
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }

    fun submitName() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getCurrentUserIdUseCase()) {
                is DomainResult.Success -> {
                    updateUserScreenName(result.data)
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = errorMessage
                    )
                }
            }
        }
    }

    private suspend fun updateUserScreenName(userId: String) {
        when (val result = updateUserUseCase(userId, UpdateUserRequest(screenName = _uiState.value.currentName))) {
            is DomainResult.Success -> {
                navigateToNextAuthScreen()
            }
            is DomainResult.Failure -> {
                val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    private suspend fun navigateToNextAuthScreen() {
        when (val result = determineAuthStatesUseCase()) {
            is DomainResult.Success -> {
                val destination = authFlowNavigationMapper.determineDestination(result.data)
                _uiEvent.emit(UiEvent.Navigate(destination))
            }
            is DomainResult.Failure -> {
                val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
}

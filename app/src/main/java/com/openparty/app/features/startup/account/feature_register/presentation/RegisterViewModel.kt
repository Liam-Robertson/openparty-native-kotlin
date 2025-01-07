package com.openparty.app.features.startup.account.feature_register.presentation

import androidx.lifecycle.viewModelScope
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.startup.account.feature_register.domain.usecase.PerformRegisterUseCase
import com.openparty.app.features.startup.account.shared.presentation.AccountViewModel
import com.openparty.app.features.startup.account.shared.presentation.model.AccountUiStateUpdate
import com.openparty.app.features.startup.feature_authentication.domain.usecase.DetermineAuthStatesUseCase
import com.openparty.app.features.startup.feature_authentication.presentation.AuthFlowNavigationMapper
import com.openparty.app.navigation.NavDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val performRegisterUseCase: PerformRegisterUseCase,
    private val determineAuthStatesUseCase: DetermineAuthStatesUseCase,
    private val authFlowNavigationMapper: AuthFlowNavigationMapper
) : AccountViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    fun onRegisterButtonClick() {
        viewModelScope.launch {
            updateState(AccountUiStateUpdate.UpdateLoading(true))
            val currentState = accountUiState.value

            when (val registerResult = performRegisterUseCase(currentState.email, currentState.password)) {
                is DomainResult.Success -> {
                    when (val authStatesResult = determineAuthStatesUseCase()) {
                        is DomainResult.Success -> {
                            val destination = authFlowNavigationMapper.determineDestination(authStatesResult.data)
                            _uiEvent.emit(UiEvent.Navigate(destination))
                        }
                        is DomainResult.Failure -> {
                            val errorMessage = AppErrorMapper.getUserFriendlyMessage(authStatesResult.error)
                            updateState(AccountUiStateUpdate.UpdateError(errorMessage))
                        }
                    }
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(registerResult.error)
                    updateState(AccountUiStateUpdate.UpdateError(errorMessage))
                }
            }
            updateState(AccountUiStateUpdate.UpdateLoading(false))
        }
    }

    fun onTextFooterClick() {
        viewModelScope.launch {
            _uiEvent.emit(UiEvent.Navigate(NavDestinations.Login))
        }
    }

    fun clearError() {
        updateState(AccountUiStateUpdate.UpdateError(null))
    }
}

package com.openparty.app.features.startup.verification.feature_email_verification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.startup.feature_authentication.domain.usecase.DetermineAuthStatesUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.SendEmailVerificationUseCase
import com.openparty.app.features.startup.feature_authentication.presentation.AuthFlowNavigationMapper
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiState
import com.openparty.app.navigation.NavDestinations
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class EmailVerificationViewModel @Inject constructor(
    private val sendEmailVerificationUseCase: SendEmailVerificationUseCase,
    private val determineAuthStatesUseCase: DetermineAuthStatesUseCase,
    private val authFlowNavigationMapper: AuthFlowNavigationMapper
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    fun onSendVerificationClick() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val sendResult = sendEmailVerificationUseCase()) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    Timber.d("Verification email sent successfully")
                }
                is DomainResult.Failure -> {
                    Timber.e(sendResult.error, "Error sending verification email")
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(sendResult.error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }

    fun onCheckEmailVerificationStatus() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val statesResult = determineAuthStatesUseCase()) {
                is DomainResult.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    val destination = authFlowNavigationMapper.determineDestination(statesResult.data)

                    if (destination == NavDestinations.EmailVerification) {
                        Timber.d("User email not verified; staying on the current screen.")
                        _uiState.value = _uiState.value.copy(errorMessage = "Email hasn't been verified yet. Check your emails for the verification email.")
                    } else {
                        Timber.d("Navigating to ${destination.route}")
                        _uiEvent.emit(UiEvent.Navigate(destination))
                    }
                }
                is DomainResult.Failure -> {
                    Timber.e(statesResult.error, "Error determining next screen")
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(statesResult.error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }
}

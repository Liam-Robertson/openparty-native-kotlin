package com.openparty.app.features.startup.verification.feature_location_verification.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.core.util.AppUtils
import com.openparty.app.features.startup.feature_authentication.domain.usecase.DetermineAuthStatesUseCase
import com.openparty.app.features.startup.feature_authentication.presentation.AuthFlowNavigationMapper
import com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase.HandleLocationPopupUseCase
import com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase.UpdateUserLocationUseCase
import com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase.VerifyLocationUseCase
import com.openparty.app.features.startup.verification.feature_location_verification.presentation.components.LocationVerificationUiEvent
import com.openparty.app.features.startup.verification.feature_location_verification.presentation.components.LocationVerificationUiState
import com.openparty.app.navigation.NavDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class LocationVerificationViewModel @Inject constructor(
    private val verifyLocationUseCase: VerifyLocationUseCase,
    private val handleLocationPopupUseCase: HandleLocationPopupUseCase,
    private val updateUserLocationUseCase: UpdateUserLocationUseCase,
    private val determineAuthStatesUseCase: DetermineAuthStatesUseCase,
    private val authFlowNavigationMapper: AuthFlowNavigationMapper
) : ViewModel() {

    private val _uiState = MutableStateFlow(LocationVerificationUiState())
    val uiState: StateFlow<LocationVerificationUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<LocationVerificationUiEvent>()
    val uiEvent: SharedFlow<LocationVerificationUiEvent> = _uiEvent

    private var permissionRequestCount = 0

    init {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(showVerificationDialog = true))
        }
    }

    fun onVerificationDialogOkClicked() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(showVerificationDialog = false))
            _uiEvent.emit(LocationVerificationUiEvent.RequestPermission(android.Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    fun onSettingsDialogClicked(context: Context) {
        AppUtils.openAppSettings(context)
    }

    fun handleLocationPopupResult(isGranted: Boolean) {
        viewModelScope.launch {
            val currentState = _uiState.value
            when (val result = handleLocationPopupUseCase.execute(isGranted, currentState, permissionRequestCount)) {
                is DomainResult.Success -> {
                    val updatedState = result.data
                    _uiState.value = updatedState
                    if (updatedState.permissionsGranted) {
                        fetchLocation()
                    } else if (updatedState.showVerificationDialog) {
                        permissionRequestCount++
                    }
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
                }
            }
        }
    }

    private fun fetchLocation() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            when (val result = verifyLocationUseCase.execute()) {
                is DomainResult.Success -> {
                    if (result.data) {
                        updateUserLocation()
                    } else {
                        _uiState.emit(
                            _uiState.value.copy(
                                showVerificationDialog = true,
                                errorMessage = "You appear to be outside West Lothian. This app is only for West Lothian residents."
                            )
                        )
                    }
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
            _uiState.emit(_uiState.value.copy(isLoading = false))
        }
    }

    private fun updateUserLocation() {
        viewModelScope.launch {
            _uiState.emit(_uiState.value.copy(isLoading = true))
            when (val result = updateUserLocationUseCase.execute()) {
                is DomainResult.Success -> {
                    navigateToNextAuthScreen()
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
            _uiState.emit(_uiState.value.copy(isLoading = false))
        }
    }

    private suspend fun navigateToNextAuthScreen() {
        when (val authStatesResult = determineAuthStatesUseCase()) {
            is DomainResult.Success -> {
                val destination = authFlowNavigationMapper.determineDestination(authStatesResult.data)
                if (destination == NavDestinations.LocationVerification) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Location verification is incomplete. Please try again.")
                    Timber.e("Already on LocationVerification. Not navigating.")
                } else {
                    _uiEvent.emit(LocationVerificationUiEvent.Navigate(destination))
                }
            }
            is DomainResult.Failure -> {
                val errorMessage = AppErrorMapper.getUserFriendlyMessage(authStatesResult.error)
                _uiState.value = _uiState.value.copy(errorMessage = errorMessage)
                Timber.e("Error determining next auth screen: $errorMessage")
            }
        }
    }

}

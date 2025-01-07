package com.openparty.app.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.core.analytics.domain.usecase.TrackAppOpenedUseCase
import com.openparty.app.core.analytics.domain.usecase.IdentifyUserUseCase
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.features.startup.feature_authentication.domain.usecase.GetCurrentUserIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val trackAppOpenedUseCase: TrackAppOpenedUseCase,
    private val identifyUserUseCase: IdentifyUserUseCase,
    private val getCurrentUserIdUseCase: GetCurrentUserIdUseCase
) : ViewModel() {

    fun trackAppOpenedAndIdentifyUser() {
        viewModelScope.launch {
            when (val userIdResult = getCurrentUserIdUseCase()) {
                is DomainResult.Success -> {
                    val userId = userIdResult.data
                    handleIdentifyUser(userId)
                    handleTrackAppOpened(userId)
                }
                is DomainResult.Failure -> {
                    handleTrackAppOpened(null)
                }
            }
        }
    }

    private suspend fun handleIdentifyUser(userId: String) {
        when (val identifyResult = identifyUserUseCase(userId)) {
            is DomainResult.Success -> Timber.i("User successfully identified: $userId")
            is DomainResult.Failure -> Timber.w("Failed to identify user: $userId")
        }
    }

    private suspend fun handleTrackAppOpened(userId: String?) {
        when (val trackResult = trackAppOpenedUseCase(userId)) {
            is DomainResult.Success -> Timber.i("App Opened event tracked for userId: $userId")
            is DomainResult.Failure -> Timber.w("Failed to track App Opened event for userId: $userId")
        }
    }
}

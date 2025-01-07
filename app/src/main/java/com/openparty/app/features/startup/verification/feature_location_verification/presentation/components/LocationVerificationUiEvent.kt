package com.openparty.app.features.startup.verification.feature_location_verification.presentation.components

import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.navigation.NavDestinations

sealed class LocationVerificationUiEvent {
    data class Navigate(val destination: NavDestinations) : LocationVerificationUiEvent()
    data class RequestPermission(val permission: String) : LocationVerificationUiEvent()
}

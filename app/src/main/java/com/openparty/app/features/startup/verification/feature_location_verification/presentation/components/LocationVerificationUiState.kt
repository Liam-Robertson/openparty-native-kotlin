package com.openparty.app.features.startup.verification.feature_location_verification.presentation.components

data class LocationVerificationUiState(
    val showVerificationDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val permissionsGranted: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null

)

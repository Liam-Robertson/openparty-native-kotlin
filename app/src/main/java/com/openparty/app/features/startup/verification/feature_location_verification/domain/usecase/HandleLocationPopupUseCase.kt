package com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.verification.feature_location_verification.presentation.components.LocationVerificationUiState
import timber.log.Timber
import javax.inject.Inject

class HandleLocationPopupUseCase @Inject constructor() {

    fun execute(
        isGranted: Boolean,
        currentState: LocationVerificationUiState,
        permissionRequestCount: Int
    ): DomainResult<LocationVerificationUiState> {
        Timber.d("Executing HandleLocationPopupUseCase with isGranted: $isGranted, permissionRequestCount: $permissionRequestCount")
        return try {
            if (isGranted) {
                Timber.d("Permissions granted, updating UI state")
                DomainResult.Success(
                    currentState.copy(
                        showVerificationDialog = false,
                        showSettingsDialog = false,
                        permissionsGranted = true,
                        isLoading = false
                    )
                )
            } else {
                val updatedCount = permissionRequestCount + 1
                Timber.d("Permissions not granted, updated request count: $updatedCount")

                if (updatedCount >= 3) {
                    Timber.d("Permission request count exceeded threshold, showing settings dialog")
                    DomainResult.Success(
                        currentState.copy(
                            showVerificationDialog = false,
                            showSettingsDialog = true,
                            permissionsGranted = false,
                            isLoading = false
                        )
                    )
                } else {
                    Timber.d("Prompting user with verification dialog")
                    DomainResult.Success(
                        currentState.copy(
                            showVerificationDialog = true,
                            showSettingsDialog = false,
                            permissionsGranted = false,
                            isLoading = false
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error occurred while handling location popup")
            DomainResult.Failure(AppError.LocationVerification.HandleLocationsPopup)
        }
    }
}

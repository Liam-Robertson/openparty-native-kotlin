package com.openparty.app.features.shared.feature_permissions.domain.usecase

import android.content.Context
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.shared.feature_permissions.domain.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class LocationPermissionCheckUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val permissionManager: PermissionManager
) {

    fun execute(): DomainResult<Boolean> {
        Timber.d("Starting LocationPermissionCheckUseCase execution")

        val fineLocationResult = permissionManager.hasPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
        Timber.d("Fine location permission check result: $fineLocationResult")

        when (fineLocationResult) {
            is DomainResult.Failure -> {
                Timber.e("Failed to check fine location permission: ${fineLocationResult.error}")
                return DomainResult.Failure(AppError.LocationVerification.LocationPermissionsError)
            }
            is DomainResult.Success -> {
                if (!fineLocationResult.data) {
                    Timber.w("Fine location permission denied by the user")
                    return DomainResult.Failure(AppError.Permissions.RefusedLocationPermissions)
                }
            }
        }

        val coarseLocationResult = permissionManager.hasPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        Timber.d("Coarse location permission check result: $coarseLocationResult")

        when (coarseLocationResult) {
            is DomainResult.Failure -> {
                Timber.e("Failed to check coarse location permission: ${coarseLocationResult.error}")
                return DomainResult.Failure(AppError.LocationVerification.LocationPermissionsError)
            }
            is DomainResult.Success -> {
                if (!coarseLocationResult.data) {
                    Timber.w("Coarse location permission denied by the user")
                    return DomainResult.Failure(AppError.Permissions.RefusedLocationPermissions)
                }
            }
        }

        Timber.d("Both fine and coarse location permissions are granted")
        return DomainResult.Success(true)
    }
}

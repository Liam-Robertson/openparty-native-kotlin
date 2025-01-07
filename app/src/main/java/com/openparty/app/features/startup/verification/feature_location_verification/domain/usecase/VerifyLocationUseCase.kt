package com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase

import com.google.android.gms.location.FusedLocationProviderClient
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.shared.feature_permissions.domain.usecase.LocationPermissionCheckUseCase
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

class VerifyLocationUseCase @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val locationPermissionCheckUseCase: LocationPermissionCheckUseCase
) {

    suspend fun execute(): DomainResult<Boolean> {
        return try {
            val permissionResult = locationPermissionCheckUseCase.execute()
            if (permissionResult is DomainResult.Failure) {
                return DomainResult.Failure(AppError.LocationVerification.LocationPermissionsError)
            }

            val location = fusedLocationClient.lastLocation.await()
                ?: return DomainResult.Failure(AppError.LocationVerification.VerifyLocation)

            val isInside = isInsideWestLothian(location.latitude, location.longitude)
            DomainResult.Success(isInside)
        } catch (e: SecurityException) {
            Timber.e(e, "Location permission is missing")
            DomainResult.Failure(AppError.LocationVerification.LocationPermissionsError)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error while accessing location")
            DomainResult.Failure(AppError.LocationVerification.VerifyLocation)
        }
    }

    private fun isInsideWestLothian(latitude: Double, longitude: Double): Boolean {
        val westLothianCenterLat = 55.908
        val westLothianCenterLon = -3.551
        val radiusKm = 20.0
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(latitude - westLothianCenterLat)
        val dLon = Math.toRadians(longitude - westLothianCenterLon)
        val a = kotlin.math.sin(dLat / 2).pow(2.0) +
                kotlin.math.cos(Math.toRadians(westLothianCenterLat)) *
                kotlin.math.cos(Math.toRadians(latitude)) *
                kotlin.math.sin(dLon / 2).pow(2.0)
        val c = 2 * kotlin.math.atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadiusKm * c
        return distance <= radiusKm
    }
}

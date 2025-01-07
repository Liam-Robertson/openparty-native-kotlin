package com.openparty.app.features.startup.verification.feature_location_verification.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.openparty.app.features.shared.feature_permissions.domain.PermissionManager
import com.openparty.app.features.shared.feature_permissions.domain.usecase.LocationPermissionCheckUseCase
import com.openparty.app.features.shared.feature_user.domain.usecase.UpdateUserUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.GetCurrentUserIdUseCase
import com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase.HandleLocationPopupUseCase
import com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase.UpdateUserLocationUseCase
import com.openparty.app.features.startup.verification.feature_location_verification.domain.usecase.VerifyLocationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocationVerificationModule {

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context
    ): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }

    @Provides
    @Singleton
    fun provideHandleLocationPermissionUseCase(): HandleLocationPopupUseCase {
        return HandleLocationPopupUseCase()
    }

    @Provides
    @Singleton
    fun provideLocationPermissionCheckUseCase(
        @ApplicationContext context: Context,
        permissionManager: PermissionManager
    ): LocationPermissionCheckUseCase {
        return LocationPermissionCheckUseCase(context, permissionManager)
    }

    @Provides
    @Singleton
    fun provideVerifyLocationUseCase(
        fusedLocationProviderClient: FusedLocationProviderClient,
        locationPermissionCheckUseCase: LocationPermissionCheckUseCase
    ): VerifyLocationUseCase {
        return VerifyLocationUseCase(fusedLocationProviderClient, locationPermissionCheckUseCase)
    }

    @Provides
    @Singleton
    fun provideUpdateUserLocationUseCase(
        getCurrentUserIdUseCase: GetCurrentUserIdUseCase,
        updateUserUseCase: UpdateUserUseCase
    ): UpdateUserLocationUseCase {
        return UpdateUserLocationUseCase(getCurrentUserIdUseCase, updateUserUseCase)
    }
}

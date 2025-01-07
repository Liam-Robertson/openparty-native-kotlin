package com.openparty.app.features.startup.feature_authentication.presentation

import com.openparty.app.features.startup.feature_authentication.domain.model.AuthState
import com.openparty.app.navigation.NavDestinations
import timber.log.Timber

class AuthFlowNavigationMapper {

    fun determineDestination(states: List<AuthState>): NavDestinations {
        Timber.i("Determining navigation destination based on auth states: $states")

        return when {
            !states.contains(AuthState.isLoggedIn) -> {
                Timber.i("User is not logged in; navigating to Login")
                NavDestinations.Login
            }
            !states.contains(AuthState.isEmailVerified) -> {
                Timber.i("User email is not verified; navigating to EmailVerification")
                NavDestinations.EmailVerification
            }
            !states.contains(AuthState.isLocationVerified) -> {
                Timber.i("User location is not verified; navigating to LocationVerification")
                NavDestinations.LocationVerification
            }
            !states.contains(AuthState.isScreenNameGenerated) -> {
                Timber.i("User screen name is not generated; navigating to ScreenNameGeneration")
                NavDestinations.ScreenNameGeneration
            }
            !states.contains(AuthState.isManuallyVerified) -> {
                Timber.i("User is not manually verified; navigating to ManualVerification")
                NavDestinations.ManualVerification
            }
            else -> {
                Timber.i("All auth states satisfied; navigating to DiscussionsPreview")
                NavDestinations.DiscussionsPreview
            }
        }
    }
}

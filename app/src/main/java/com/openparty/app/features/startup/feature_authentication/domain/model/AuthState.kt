package com.openparty.app.features.startup.feature_authentication.domain.model

sealed class AuthState(val name: String) {
    object isLoggedIn : AuthState("isLoggedIn")
    object isEmailVerified : AuthState("isEmailVerified")
    object isLocationVerified : AuthState("isLocationVerified")
    object isScreenNameGenerated : AuthState("isScreenNameGenerated")
    object isManuallyVerified : AuthState("isManuallyVerified")
}

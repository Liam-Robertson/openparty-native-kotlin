package com.openparty.app.features.startup.feature_authentication.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import timber.log.Timber
import javax.inject.Inject

class GetFirebaseUserUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(): DomainResult<FirebaseUser> {
        Timber.i("GetCurrentUserUseCase invoked")

        return try {
            Timber.d("Fetching current user from authentication repository")
            val user = authenticationRepository.getCurrentUser()

            if (user != null) {
                Timber.i("Current user retrieved successfully: UID=${user.uid}")
                DomainResult.Success(user)
            } else {
                Timber.w("No current user found; returning failure")
                DomainResult.Failure(AppError.Authentication.GetUser)
            }
        } catch (e: Throwable) {
            Timber.e(e, "Unexpected error while fetching current user")
            DomainResult.Failure(AppError.Authentication.GetUser)
        }
    }
}

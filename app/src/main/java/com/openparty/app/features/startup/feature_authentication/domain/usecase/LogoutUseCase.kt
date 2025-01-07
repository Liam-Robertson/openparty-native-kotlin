package com.openparty.app.features.startup.feature_authentication.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import timber.log.Timber
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        Timber.i("LogoutUseCase invoked")

        return try {
            Timber.d("Attempting to log out user")
            val result = authenticationRepository.logout()

            when (result) {
                is DomainResult.Success -> {
                    Timber.i("User logged out successfully")
                    DomainResult.Success(Unit)
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to log out user: ${result.error}")
                    DomainResult.Failure(result.error)
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Unexpected error during logout")
            DomainResult.Failure(AppError.Authentication.Logout)
        }
    }
}

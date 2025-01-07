package com.openparty.app.features.startup.feature_authentication.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import timber.log.Timber
import javax.inject.Inject

class RefreshAccessTokenUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(): DomainResult<String> {
        Timber.i("RefreshAccessTokenUseCase invoked")

        return try {
            Timber.d("Attempting to refresh access token")
            when (val result = authenticationRepository.refreshAccessToken()) {
                is DomainResult.Success -> {
                    Timber.i("Access token refreshed successfully: ${result.data}")
                    result
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to refresh access token: ${result.error}")
                    DomainResult.Failure(result.error)
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Unexpected error during access token refresh")
            DomainResult.Failure(AppError.Authentication.RefreshToken)
        }
    }
}

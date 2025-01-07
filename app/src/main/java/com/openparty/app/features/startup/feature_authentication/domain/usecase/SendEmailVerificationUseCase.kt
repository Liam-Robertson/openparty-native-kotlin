package com.openparty.app.features.startup.feature_authentication.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import javax.inject.Inject

class SendEmailVerificationUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(): DomainResult<Unit> {
        return try {
            when (val result = authenticationRepository.sendEmailVerification()) {
                is DomainResult.Success -> result
                is DomainResult.Failure -> DomainResult.Failure(result.error)
            }
        } catch (e: Throwable) {
            DomainResult.Failure(AppError.Authentication.EmailVerification)
        }
    }
}

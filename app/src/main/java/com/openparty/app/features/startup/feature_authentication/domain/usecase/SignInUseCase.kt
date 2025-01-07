package com.openparty.app.features.startup.feature_authentication.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository
) {
    suspend operator fun invoke(email: String, password: String): DomainResult<Unit> {
        return try {
            when (val result = authenticationRepository.login(email, password)) {
                is DomainResult.Success -> result
                is DomainResult.Failure -> DomainResult.Failure(result.error)
            }
        } catch (e: Throwable) {
            DomainResult.Failure(AppError.Authentication.SignIn)
        }
    }
}

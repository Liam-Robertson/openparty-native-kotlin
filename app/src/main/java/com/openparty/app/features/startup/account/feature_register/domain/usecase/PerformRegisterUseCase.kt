package com.openparty.app.features.startup.account.feature_register.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.account.shared.domain.usecase.ValidateCredentialsUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.DetermineAuthStatesUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.RegisterUseCase
import timber.log.Timber
import javax.inject.Inject

class PerformRegisterUseCase @Inject constructor(
private val validateCredentialsUseCase: ValidateCredentialsUseCase,
private val registerUseCase: RegisterUseCase,
private val determineAuthStatesUseCase: DetermineAuthStatesUseCase
) {
    suspend operator fun invoke(email: String, password: String): DomainResult<Unit> {
        return try {
            Timber.d("Starting PerformRegisterUseCase with email: $email")

            when (val validateResult = validateCredentialsUseCase(email, password)) {
                is DomainResult.Failure -> {
                    Timber.e("Validation failed: $validateResult")
                    return validateResult
                }
                is DomainResult.Success -> {
                    Timber.d("Validation succeeded")
                }
            }

            when (val registerResult = registerUseCase(email, password)) {
                is DomainResult.Failure -> {
                    Timber.e("Registration failed: $registerResult")
                    return registerResult
                }
                is DomainResult.Success -> {
                    Timber.d("Registration succeeded")

                    when (val authStatesResult = determineAuthStatesUseCase()) {
                        is DomainResult.Failure -> {
                            Timber.e("DetermineAuthStatesUseCase failed: $authStatesResult")
                            return authStatesResult
                        }
                        is DomainResult.Success -> {
                            Timber.d("DetermineAuthStatesUseCase succeeded: ${authStatesResult.data}")
                        }
                    }
                }
            }

            Timber.i("PerformRegisterUseCase completed successfully")
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error in PerformRegisterUseCase")
            DomainResult.Failure(AppError.Authentication.Register)
        }
    }
}

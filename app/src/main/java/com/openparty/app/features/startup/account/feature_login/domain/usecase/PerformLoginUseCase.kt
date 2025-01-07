
package com.openparty.app.features.startup.account.feature_login.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.features.startup.account.shared.domain.usecase.ValidateCredentialsUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.SignInUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class PerformLoginUseCase @Inject constructor(
    private val validateCredentialsUseCase: ValidateCredentialsUseCase,
    private val signInUseCase: SignInUseCase
) {
    suspend operator fun invoke(email: String, password: String): DomainResult<Unit> {
        Timber.i("PerformLoginUseCase invoked with email: $email")

        return withContext(Dispatchers.IO) {
            Timber.d("Validating credentials")
            when (val validationResult = validateCredentialsUseCase(email, password)) {
                is DomainResult.Failure -> {
                    Timber.e("Validation failed: ${validationResult.error}")
                    return@withContext validationResult
                }
                is DomainResult.Success -> {
                    Timber.i("Validation successful for email: $email")
                }
            }

            Timber.d("Attempting to sign in")
            when (val signInResult = signInUseCase(email, password)) {
                is DomainResult.Success -> {
                    Timber.i("Sign in successful for email: $email")
                    DomainResult.Success(Unit)
                }
                is DomainResult.Failure -> {
                    Timber.e("Sign in failed: ${signInResult.error}")
                    signInResult
                }
            }
        }
    }
}

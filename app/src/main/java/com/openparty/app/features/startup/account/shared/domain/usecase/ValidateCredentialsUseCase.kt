package com.openparty.app.features.startup.account.shared.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import timber.log.Timber

class ValidateCredentialsUseCase {

    operator fun invoke(email: String, password: String): DomainResult<Unit> {
        Timber.d("Validation started for email: $email")

        if (email.isBlank() || password.isBlank()) {
            Timber.e("Validation failed: Email or password is blank")
            return DomainResult.Failure(AppError.Register.ValidateEmail)
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Timber.e("Validation failed: Invalid email format - $email")
            return DomainResult.Failure(AppError.Register.ValidateEmail)
        }

        if (password.length < 6) {
            Timber.e("Validation failed: Password length is less than 6 characters")
            return DomainResult.Failure(AppError.Register.ValidatePassword)
        }

        Timber.d("Validation succeeded for email: $email")
        return DomainResult.Success(Unit)
    }
}

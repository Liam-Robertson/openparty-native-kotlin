package com.openparty.app.features.startup.feature_authentication.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import timber.log.Timber
import javax.inject.Inject

class GetCurrentUserIdUseCase @Inject constructor(
    private val getFirebaseUserUseCase: GetFirebaseUserUseCase
) {
    suspend operator fun invoke(): DomainResult<String> {
        Timber.i("GetCurrentUserIdUseCase invoked")
        return try {
            Timber.d("Fetching current user")
            when (val userResult = getFirebaseUserUseCase()) {
                is DomainResult.Success -> {
                    Timber.i("User fetched successfully: UID=${userResult.data.uid}")
                    DomainResult.Success(userResult.data.uid)
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to fetch user: ${userResult.error}")
                    DomainResult.Failure(AppError.Authentication.GetUserId)
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Unexpected error while fetching user ID")
            DomainResult.Failure(AppError.Authentication.GetUserId)
        }
    }
}

package com.openparty.app.features.startup.feature_screen_name_generation.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.shared.feature_user.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class IsScreenNameTakenUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(screenName: String): DomainResult<Boolean> {
        Timber.d("Checking if screen name '$screenName' is taken")
        return try {
            when (val result = userRepository.isScreenNameTaken(screenName)) {
                is DomainResult.Success -> {
                    Timber.d("Screen name '$screenName' is ${if (result.data) "taken" else "available"}")
                    DomainResult.Success(result.data)
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to check screen name availability for '$screenName': ${result.error}")
                    DomainResult.Failure(AppError.ScreenNameGeneration.ScreenNameTaken)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception occurred while checking screen name '$screenName'")
            DomainResult.Failure(AppError.ScreenNameGeneration.ScreenNameTaken)
        }
    }
}

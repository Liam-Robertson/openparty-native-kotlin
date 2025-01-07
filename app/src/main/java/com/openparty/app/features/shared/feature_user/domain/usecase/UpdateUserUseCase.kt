package com.openparty.app.features.shared.feature_user.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.shared.feature_user.domain.model.UpdateUserRequest
import com.openparty.app.features.shared.feature_user.domain.repository.UserRepository
import timber.log.Timber
import javax.inject.Inject

class UpdateUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String, request: UpdateUserRequest): DomainResult<Unit> {
        Timber.d("UpdateUserUseCase invoked with userId: $userId and request: $request")
        return try {
            when (val result = userRepository.updateUser(userId, request)) {
                is DomainResult.Success -> {
                    Timber.d("Successfully updated user with userId: $userId")
                    result
                }
                is DomainResult.Failure -> {
                    Timber.w("Failed to update user with userId: $userId")
                    DomainResult.Failure(AppError.User.UpdateUserUseCase)
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Exception occurred while updating user with userId: $userId")
            DomainResult.Failure(AppError.User.UpdateUserUseCase)
        }
    }
}

package com.openparty.app.features.shared.feature_user.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.shared.feature_user.domain.model.User
import com.openparty.app.features.shared.feature_user.domain.repository.UserRepository
import com.openparty.app.features.startup.feature_authentication.domain.usecase.GetFirebaseUserUseCase
import timber.log.Timber
import javax.inject.Inject

class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val getFirebaseUserUseCase: GetFirebaseUserUseCase
) {
    suspend operator fun invoke(): DomainResult<User> {
        Timber.d("GetUserUseCase invoked")
        return try {
            when (val firebaseUserResult = getFirebaseUserUseCase()) {
                is DomainResult.Success -> {
                    val userId = firebaseUserResult.data.uid
                    Timber.d("Successfully retrieved FirebaseUser with UID: $userId")
                    when (val userResult = userRepository.getUser(userId)) {
                        is DomainResult.Success -> {
                            Timber.d("Successfully fetched user with userId: $userId")
                            DomainResult.Success(userResult.data)
                        }
                        is DomainResult.Failure -> {
                            Timber.w("Failed to fetch user with userId: $userId")
                            DomainResult.Failure(AppError.Authentication.GetUser)
                        }
                    }
                }
                is DomainResult.Failure -> {
                    Timber.w("Failed to retrieve FirebaseUser")
                    DomainResult.Failure(AppError.Authentication.GetUser)
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Exception occurred while fetching user")
            DomainResult.Failure(AppError.Authentication.GetUser)
        }
    }
}

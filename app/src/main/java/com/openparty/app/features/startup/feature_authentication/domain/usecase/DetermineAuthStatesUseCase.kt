package com.openparty.app.features.startup.feature_authentication.domain.usecase

import com.google.firebase.auth.FirebaseUser
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.shared.feature_user.domain.model.User
import com.openparty.app.features.startup.feature_authentication.domain.model.AuthState
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import com.openparty.app.features.shared.feature_user.domain.usecase.GetUserUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class DetermineAuthStatesUseCase @Inject constructor(
    private val authenticationRepository: AuthenticationRepository,
    private val getUserUseCase: GetUserUseCase
) {
    suspend operator fun invoke(): DomainResult<List<AuthState>> {
        return try {
            withContext(Dispatchers.IO) {
                Timber.d("Invoking DetermineAuthStatesUseCase")
                val firebaseUser = getFirebaseUser() ?: return@withContext DomainResult.Success(emptyList())
                reloadFirebaseUser(firebaseUser)
                determineAuthStates(firebaseUser)
            }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error in DetermineAuthStatesUseCase")
            DomainResult.Failure(AppError.Navigation.DetermineAuthStates)
        }
    }

    private suspend fun determineAuthStates(firebaseUser: FirebaseUser): DomainResult<List<AuthState>> {
        val states = mutableListOf<AuthState>()
        // If user is not logged in, return failure
        val domainUser = getUserDetails(firebaseUser.uid) ?: return DomainResult.Failure(AppError.Navigation.DetermineAuthStates)
        
        states.add(AuthState.isLoggedIn)
        if (!firebaseUser.isEmailVerified) {
            Timber.d("User is logged in but email is not verified.")
            return DomainResult.Success(states)
        }
        states.add(AuthState.isEmailVerified)

        if (!domainUser.isLocationVerified) {
            Timber.d("Location not verified.")
            return DomainResult.Success(states)
        }
        states.add(AuthState.isLocationVerified)

        if (domainUser.screenName.isBlank()) {
            Timber.d("Screen name not generated.")
            return DomainResult.Success(states)
        }
        states.add(AuthState.isScreenNameGenerated)

        if (!domainUser.manuallyVerified) {
            Timber.d("User not manually verified.")
            return DomainResult.Success(states)
        }
        states.add(AuthState.isManuallyVerified)

        Timber.d("All checks passed. Determined auth states: $states")
        return DomainResult.Success(states)
    }

    private suspend fun getFirebaseUser(): FirebaseUser? {
        return try {
            authenticationRepository.observeAuthState().firstOrNull().also {
                if (it == null) Timber.d("No Firebase user found.")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error observing auth state")
            null
        }
    }

    private suspend fun reloadFirebaseUser(firebaseUser: FirebaseUser): Boolean {
        return try {
            Timber.d("Reloading Firebase user data")
            firebaseUser.reload()
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to reload Firebase user data")
            false
        }
    }

    private suspend fun getUserDetails(userId: String): User? {
        return try {
            when (val result = getUserUseCase()) {
                is DomainResult.Success -> result.data.also {
                    Timber.d("User details fetched successfully for userId: $userId")
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to fetch user details for userId: $userId")
                    null
                }
            }
        } catch (e: Throwable) {
            Timber.e(e, "Unexpected error fetching user details for userId: $userId")
            null
        }
    }
}

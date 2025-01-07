package com.openparty.app.features.startup.feature_authentication.data

import com.google.firebase.auth.FirebaseUser
import com.openparty.app.core.storage.SecureStorage
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.startup.feature_authentication.data.datasource.AuthDataSource
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthenticationRepositoryImpl @Inject constructor(
    private val authDataSource: AuthDataSource,
    private val secureStorage: SecureStorage
) : AuthenticationRepository {

    override suspend fun login(email: String, password: String): DomainResult<Unit> {
        Timber.d("Login invoked with email: $email")
        return try {
            val user = authDataSource.signIn(email, password)
            Timber.d("Login successful for email: $email, userId: ${user.uid}")
            val token = authDataSource.getToken(user)
            secureStorage.saveToken(token)
            Timber.d("Token saved successfully for userId: ${user.uid}")
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Login failed for email: $email")
            DomainResult.Failure(AppError.Authentication.General)
        }
    }

    override suspend fun register(email: String, password: String): DomainResult<String> {
        Timber.d("Register invoked with email: $email")
        return try {
            val user = authDataSource.register(email, password)
            Timber.d("Registration successful for email: $email, userId: ${user.uid}")
            val token = authDataSource.getToken(user)
            secureStorage.saveToken(token)
            Timber.d("Token saved successfully for userId: ${user.uid}")
            DomainResult.Success(user.uid)
        } catch (e: AppError.Authentication.UserAlreadyExists) {
            Timber.e("Registration failed: User already exists for email: $email")
            DomainResult.Failure(AppError.Authentication.UserAlreadyExists)
        } catch (e: Exception) {
            Timber.e(e, "Registration failed for email: $email")
            DomainResult.Failure(AppError.Authentication.General)
        }
    }

    override suspend fun sendEmailVerification(): DomainResult<Unit> {
        Timber.d("SendEmailVerification invoked")
        val currentUser = authDataSource.currentUser()
        if (currentUser == null) {
            Timber.w("SendEmailVerification failed: No current user found")
            return DomainResult.Failure(AppError.Authentication.General)
        }
        return try {
            authDataSource.sendVerificationEmail(currentUser)
            Timber.d("Verification email sent successfully to userId: ${currentUser.uid}")
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send verification email to userId: ${currentUser.uid}")
            DomainResult.Failure(AppError.Authentication.General)
        }
    }

    override fun observeAuthState(): Flow<FirebaseUser?> {
        Timber.d("ObserveAuthState invoked")
        return try {
            authDataSource.authStateFlow()
        } catch (e: Exception) {
            Timber.e(e, "Error in observeAuthState")
            throw Exception("Failed to observe auth state.", e)
        }
    }

    override suspend fun logout(): DomainResult<Unit> {
        Timber.d("Logout invoked")
        return try {
            authDataSource.signOut()
            secureStorage.clearToken()
            Timber.d("User logged out and token cleared successfully")
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Logout failed")
            DomainResult.Failure(AppError.Authentication.General)
        }
    }

    override suspend fun getCurrentUser(): FirebaseUser? {
        Timber.d("GetCurrentUser invoked")
        return try {
            val user = authDataSource.currentUser()
            Timber.d("Current user fetched: $user")
            user
        } catch (e: Exception) {
            Timber.e(e, "Error fetching current user")
            null
        }
    }

    override suspend fun refreshAccessToken(): DomainResult<String> {
        Timber.d("RefreshAccessToken invoked")
        val user = authDataSource.currentUser()
        if (user == null) {
            Timber.w("RefreshAccessToken failed: No current user found")
            return DomainResult.Failure(AppError.Authentication.General)
        }
        return try {
            val token = authDataSource.getToken(user)
            secureStorage.saveToken(token)
            Timber.d("Access token refreshed and saved successfully for userId: ${user.uid}")
            DomainResult.Success(token)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh access token for userId: ${user.uid}")
            DomainResult.Failure(AppError.Authentication.General)
        }
    }
}

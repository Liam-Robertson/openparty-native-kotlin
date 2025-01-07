package com.openparty.app.features.startup.feature_authentication.domain.repository

import com.google.firebase.auth.FirebaseUser
import com.openparty.app.core.shared.domain.DomainResult
import kotlinx.coroutines.flow.Flow

interface AuthenticationRepository {
    suspend fun login(email: String, password: String): DomainResult<Unit>
    suspend fun register(email: String, password: String): DomainResult<String>
    suspend fun sendEmailVerification(): DomainResult<Unit>
    fun observeAuthState(): Flow<FirebaseUser?>
    suspend fun logout(): DomainResult<Unit>
    suspend fun getCurrentUser(): FirebaseUser?
    suspend fun refreshAccessToken(): DomainResult<String>
}

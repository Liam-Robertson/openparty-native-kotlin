package com.openparty.app.features.startup.feature_authentication.data.datasource

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthDataSource {
    suspend fun signIn(email: String, password: String): FirebaseUser
    suspend fun register(email: String, password: String): FirebaseUser
    suspend fun sendVerificationEmail(user: FirebaseUser)
    fun authStateFlow(): Flow<FirebaseUser?>
    fun currentUser(): FirebaseUser?
    suspend fun getToken(user: FirebaseUser): String
    fun signOut()
}

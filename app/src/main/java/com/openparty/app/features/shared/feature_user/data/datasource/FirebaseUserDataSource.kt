package com.openparty.app.features.shared.feature_user.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.openparty.app.features.shared.feature_user.data.model.UserDto
import com.openparty.app.features.shared.feature_user.domain.model.UpdateUserRequest
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) : UserDataSource {

    override suspend fun fetchUser(userId: String): UserDto {
        Timber.d("Fetching user with userId: %s", userId)
        return try {
            val snapshot = firestore.collection("users").document(userId).get().await()
            val userDto = snapshot.toObject(UserDto::class.java)
            if (userDto == null) {
                Timber.e("User not found or data is null for userId: %s", userId)
                throw IllegalStateException("User data is null or could not be mapped for userId: $userId")
            }
            userDto
        } catch (e: IllegalStateException) {
            Timber.e(e, "Error while fetching user: %s", userId)
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error occurred while fetching user: %s", userId)
            throw RuntimeException("Failed to fetch user for userId: $userId", e)
        }
    }

    override suspend fun isScreenNameTaken(name: String): Boolean {
        Timber.d("Checking if screen name is taken: %s", name)
        return try {
            val snapshot = firestore.collection("users")
                .whereEqualTo("screenName", name)
                .get()
                .await()
            snapshot.documents.isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "Error checking if screen name is taken: %s", name)
            throw RuntimeException("Failed to check if screen name is taken: $name", e)
        }
    }

    override suspend fun updateUser(userId: String, request: Any) {
        Timber.d("Updating user with userId: %s", userId)
        try {
            if (request is UpdateUserRequest) {
                val updates = mutableMapOf<String, Any>()
                request.location?.let { updates["location"] = it }
                request.locationVerified?.let { updates["locationVerified"] = it }
                request.screenName?.let { updates["screenName"] = it }
                if (updates.isNotEmpty()) {
                    firestore.collection("users").document(userId).update(updates).await()
                    Timber.d("Successfully updated user with userId: %s", userId)
                } else {
                    Timber.d("No updates to apply for userId: %s", userId)
                }
            } else {
                Timber.e("Invalid update request type for userId: %s", userId)
                throw IllegalArgumentException("Invalid update request type for userId: $userId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating user: %s", userId)
            throw RuntimeException("Failed to update user for userId: $userId", e)
        }
    }

    override suspend fun addUser(userId: String, user: UserDto) {
        Timber.d("Adding new user with userId: %s", userId)
        try {
            firestore.collection("users").document(userId).set(user).await()
            Timber.d("Successfully added user with userId: %s", userId)
        } catch (e: Exception) {
            Timber.e(e, "Error adding user: %s", userId)
            throw RuntimeException("Failed to add user for userId: $userId", e)
        }
    }
}

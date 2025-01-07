package com.openparty.app.core.firebase.feature_firebase_storage.data.repository

import com.google.firebase.storage.FirebaseStorage
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.firebase.feature_firebase_storage.domain.repository.FirebaseStorageRepository
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

class FirebaseStorageRepositoryImpl @Inject constructor(
    private val firebaseStorage: FirebaseStorage
) : FirebaseStorageRepository {
    override suspend fun resolveFirebaseUrl(gsUrl: String): DomainResult<String> {
        Timber.d("Resolving Firebase URL: $gsUrl")
        return if (gsUrl.startsWith("gs://")) {
            try {
                val storageRef = firebaseStorage.getReferenceFromUrl(gsUrl)
                Timber.d("Fetching download URL from Firebase for: $gsUrl")
                val downloadUrl = storageRef.downloadUrl.await().toString()
                Timber.d("Successfully fetched download URL: $downloadUrl")
                DomainResult.Success(downloadUrl)
            } catch (e: Exception) {
                Timber.e(e, "Error resolving Firebase URL: $gsUrl")
                DomainResult.Failure(AppError.CouncilMeeting.General)
            }
        } else {
            Timber.d("Provided URL is not a Firebase Storage URL: $gsUrl")
            DomainResult.Success(gsUrl)
        }
    }
}

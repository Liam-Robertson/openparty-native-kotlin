package com.openparty.app.core.firebase.feature_firebase_storage.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.firebase.feature_firebase_storage.domain.repository.FirebaseStorageRepository
import timber.log.Timber
import javax.inject.Inject

class ResolveUrlUseCase @Inject constructor(
    private val firebaseStorageRepository: FirebaseStorageRepository
) {
    suspend operator fun invoke(gsUrl: String): DomainResult<String> {
        Timber.d("Attempting to resolve URL: $gsUrl")
        return try {
            when (val result = firebaseStorageRepository.resolveFirebaseUrl(gsUrl)) {
                is DomainResult.Success -> {
                    Timber.d("Successfully resolved URL: ${result.data}")
                    result
                }
                is DomainResult.Failure -> {
                    Timber.e("Error occurred while resolving URL: $gsUrl")
                    DomainResult.Failure(AppError.CouncilMeeting.General)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error while resolving URL: $gsUrl")
            DomainResult.Failure(AppError.CouncilMeeting.General)
        }
    }
}

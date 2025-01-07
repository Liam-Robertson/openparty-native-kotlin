package com.openparty.app.core.firebase.feature_firebase_storage.domain.repository

import com.openparty.app.core.shared.domain.DomainResult

interface FirebaseStorageRepository {
    suspend fun resolveFirebaseUrl(gsUrl: String): DomainResult<String>
}

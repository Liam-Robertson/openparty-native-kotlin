package com.openparty.app.core.firebase.feature_firebase_storage.di

import com.google.firebase.storage.FirebaseStorage
import com.openparty.app.core.firebase.feature_firebase_storage.data.repository.FirebaseStorageRepositoryImpl
import com.openparty.app.core.firebase.feature_firebase_storage.domain.repository.FirebaseStorageRepository
import com.openparty.app.core.firebase.feature_firebase_storage.domain.usecase.ResolveUrlUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FirebaseStorageModule {

    @Binds
    @Singleton
    abstract fun bindFirebaseStorageRepository(
        impl: FirebaseStorageRepositoryImpl
    ): FirebaseStorageRepository
}

@Module
@InstallIn(SingletonComponent::class)
object FirebaseStorageProviderModule {

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }

    @Provides
    @Singleton
    fun provideResolveUrlUseCase(
        firebaseStorageRepository: FirebaseStorageRepository
    ): ResolveUrlUseCase {
        return ResolveUrlUseCase(firebaseStorageRepository)
    }
}

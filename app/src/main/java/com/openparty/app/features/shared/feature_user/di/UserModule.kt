package com.openparty.app.features.shared.feature_user.di

import com.google.firebase.firestore.FirebaseFirestore
import com.openparty.app.features.shared.feature_user.data.repository.UserRepositoryImpl
import com.openparty.app.features.shared.feature_user.data.datasource.FirebaseUserDataSource
import com.openparty.app.features.shared.feature_user.data.datasource.UserDataSource
import com.openparty.app.features.shared.feature_user.domain.repository.UserRepository
import com.openparty.app.features.shared.feature_user.domain.usecase.GetUserUseCase
import com.openparty.app.features.shared.feature_user.domain.usecase.UpdateUserUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.GetFirebaseUserUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UserModule {

    @Provides
    @Singleton
    fun provideUserDataSource(
        firestore: FirebaseFirestore
    ): UserDataSource {
        return FirebaseUserDataSource(firestore)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDataSource: UserDataSource
    ): UserRepository {
        return UserRepositoryImpl(userDataSource)
    }

    @Provides
    @Singleton
    fun provideGetUserUseCase(
        userRepository: UserRepository,
        getFirebaseUserUseCase: GetFirebaseUserUseCase
    ): GetUserUseCase {
        return GetUserUseCase(userRepository, getFirebaseUserUseCase)
    }

    @Provides
    @Singleton
    fun provideUpdateUserUseCase(
        userRepository: UserRepository
    ): UpdateUserUseCase {
        return UpdateUserUseCase(userRepository)
    }
}

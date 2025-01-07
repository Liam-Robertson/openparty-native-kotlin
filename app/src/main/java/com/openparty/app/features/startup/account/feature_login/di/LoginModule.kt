package com.openparty.app.features.startup.account.feature_login.di

import com.openparty.app.features.startup.account.feature_login.domain.usecase.PerformLoginUseCase
import com.openparty.app.features.startup.account.shared.domain.usecase.ValidateCredentialsUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.SignInUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LoginModule {

    @Provides
    @Singleton
    fun providePerformLoginUseCase(
        validateCredentialsUseCase: ValidateCredentialsUseCase,
        signInUseCase: SignInUseCase
    ): PerformLoginUseCase {
        return PerformLoginUseCase(validateCredentialsUseCase, signInUseCase)
    }
}

package com.openparty.app.features.startup.account.feature_register.di

import com.openparty.app.features.startup.account.feature_register.domain.usecase.PerformRegisterUseCase
import com.openparty.app.features.startup.account.shared.domain.usecase.ValidateCredentialsUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.DetermineAuthStatesUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.RegisterUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RegisterModule {

    @Provides
    @Singleton
    fun providePerformRegisterUseCase(
        validateCredentialsUseCase: ValidateCredentialsUseCase,
        registerUseCase: RegisterUseCase,
        determineAuthStatesUseCase: DetermineAuthStatesUseCase
    ): PerformRegisterUseCase {
        return PerformRegisterUseCase(validateCredentialsUseCase, registerUseCase, determineAuthStatesUseCase)
    }
}

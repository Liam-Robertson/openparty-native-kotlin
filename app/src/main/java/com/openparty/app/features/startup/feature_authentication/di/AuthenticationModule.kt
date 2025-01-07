package com.openparty.app.features.startup.feature_authentication.di

import com.openparty.app.features.shared.feature_user.domain.repository.UserRepository
import com.openparty.app.features.shared.feature_user.domain.usecase.GetUserUseCase
import com.openparty.app.features.startup.account.shared.domain.usecase.ValidateCredentialsUseCase
import com.openparty.app.features.startup.feature_authentication.data.AuthenticationRepositoryImpl
import com.openparty.app.features.startup.feature_authentication.data.datasource.AuthDataSource
import com.openparty.app.features.startup.feature_authentication.data.datasource.FirebaseAuthDataSource
import com.openparty.app.features.startup.feature_authentication.domain.repository.AuthenticationRepository
import com.openparty.app.features.startup.feature_authentication.domain.usecase.*
import com.openparty.app.features.startup.feature_authentication.presentation.AuthFlowNavigationMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModule {

    @Binds
    @Singleton
    abstract fun bindAuthenticationRepository(
        authenticationRepositoryImpl: AuthenticationRepositoryImpl
    ): AuthenticationRepository

    @Binds
    @Singleton
    abstract fun bindAuthDataSource(
        firebaseAuthDataSource: FirebaseAuthDataSource
    ): AuthDataSource

    companion object {

        @Provides
        @Singleton
        fun provideValidateCredentialsUseCase(): ValidateCredentialsUseCase {
            return ValidateCredentialsUseCase()
        }

        @Provides
        @Singleton
        fun provideSignInUseCase(
            authenticationRepository: AuthenticationRepository
        ): SignInUseCase {
            return SignInUseCase(authenticationRepository)
        }

        @Provides
        @Singleton
        fun provideRegisterUseCase(
            authenticationRepository: AuthenticationRepository,
            userRepository: UserRepository
        ): RegisterUseCase {
            return RegisterUseCase(authenticationRepository, userRepository)
        }

        @Provides
        @Singleton
        fun provideSendEmailVerificationUseCase(
            authenticationRepository: AuthenticationRepository
        ): SendEmailVerificationUseCase {
            return SendEmailVerificationUseCase(authenticationRepository)
        }

        @Provides
        @Singleton
        fun provideLogoutUseCase(
            authenticationRepository: AuthenticationRepository
        ): LogoutUseCase {
            return LogoutUseCase(authenticationRepository)
        }

        @Provides
        @Singleton
        fun provideRefreshAccessTokenUseCase(
            authenticationRepository: AuthenticationRepository
        ): RefreshAccessTokenUseCase {
            return RefreshAccessTokenUseCase(authenticationRepository)
        }

        @Provides
        @Singleton
        fun provideDetermineAuthStatesUseCase(
            authenticationRepository: AuthenticationRepository,
            getUserUseCase: GetUserUseCase
        ): DetermineAuthStatesUseCase {
            return DetermineAuthStatesUseCase(authenticationRepository, getUserUseCase)
        }

        @Provides
        @Singleton
        fun provideAuthFlowNavigationMapper(): AuthFlowNavigationMapper {
            return AuthFlowNavigationMapper()
        }
    }
}

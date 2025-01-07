package com.openparty.app.di

import com.openparty.app.core.storage.SecureStorage
import com.openparty.app.core.network.TokenInterceptor
import com.openparty.app.features.startup.feature_authentication.domain.usecase.RefreshAccessTokenUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideTokenInterceptor(
        secureStorage: SecureStorage,
        refreshAccessTokenUseCase: RefreshAccessTokenUseCase
    ): TokenInterceptor {
        return TokenInterceptor(secureStorage, refreshAccessTokenUseCase)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        tokenInterceptor: TokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(tokenInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}

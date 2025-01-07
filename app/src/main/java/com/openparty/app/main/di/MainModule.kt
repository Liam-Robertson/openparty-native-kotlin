package com.openparty.app.main.di

import com.openparty.app.core.analytics.domain.usecase.TrackAppOpenedUseCase
import com.openparty.app.core.analytics.domain.usecase.IdentifyUserUseCase
import com.openparty.app.features.startup.feature_authentication.domain.usecase.GetCurrentUserIdUseCase
import com.openparty.app.main.MainViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {

    @Provides
    @Singleton
    fun provideMainViewModel(
        trackAppOpenedUseCase: TrackAppOpenedUseCase,
        identifyUserUseCase: IdentifyUserUseCase,
        getCurrentUserIdUseCase: GetCurrentUserIdUseCase
    ): MainViewModel {
        return MainViewModel(
            trackAppOpenedUseCase = trackAppOpenedUseCase,
            identifyUserUseCase = identifyUserUseCase,
            getCurrentUserIdUseCase = getCurrentUserIdUseCase
        )
    }
}

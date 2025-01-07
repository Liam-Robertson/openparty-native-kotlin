package com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.di

import android.content.Context
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.PlaybackManager
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.usecase.PauseAudioUseCase
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.usecase.PlayAudioUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PlaybackModule {

    @Provides
    @Singleton
    fun providePlaybackManager(
        @ApplicationContext context: Context
    ): PlaybackManager {
        return PlaybackManager(context)
    }
}

@Module
@InstallIn(ViewModelComponent::class)
object PlaybackUseCaseModule {

    @Provides
    fun providePlayAudioUseCase(
        playbackManager: PlaybackManager
    ): PlayAudioUseCase {
        return PlayAudioUseCase(playbackManager)
    }

    @Provides
    fun providePauseAudioUseCase(
        playbackManager: PlaybackManager
    ): PauseAudioUseCase {
        return PauseAudioUseCase(playbackManager)
    }
}

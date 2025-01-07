package com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_preview.di

import com.openparty.app.features.newsfeed.council_meetings.shared.domain.repository.CouncilMeetingRepository
import com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_preview.domain.usecase.GetCouncilMeetingsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CouncilMeetingsUseCaseModule {

    @Provides
    @Singleton
    fun provideGetCouncilMeetingsUseCase(
        councilMeetingRepository: CouncilMeetingRepository
    ): GetCouncilMeetingsUseCase {
        return GetCouncilMeetingsUseCase(councilMeetingRepository)
    }

}

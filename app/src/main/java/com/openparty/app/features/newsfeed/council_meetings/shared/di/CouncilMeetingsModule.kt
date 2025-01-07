package com.openparty.app.features.newsfeed.council_meetings.shared.di

import com.openparty.app.features.newsfeed.council_meetings.shared.data.repository.CouncilMeetingRepositoryImpl
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.repository.CouncilMeetingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CouncilMeetingsModule {

    @Binds
    @Singleton
    abstract fun bindCouncilMeetingRepository(
        implementation: CouncilMeetingRepositoryImpl
    ): CouncilMeetingRepository

}

package com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.di

import com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository.DiscussionRepository
import com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.domain.usecase.GetDiscussionsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DiscussionsUseCaseModule {

    @Provides
    @Singleton
    fun provideGetDiscussionsUseCase(
        discussionRepository: DiscussionRepository
    ): GetDiscussionsUseCase {
        return GetDiscussionsUseCase(discussionRepository)
    }
}

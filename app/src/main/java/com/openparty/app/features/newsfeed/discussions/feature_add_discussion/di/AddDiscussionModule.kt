package com.openparty.app.features.newsfeed.discussions.feature_add_discussion.di

import com.openparty.app.features.newsfeed.discussions.feature_add_discussion.domain.usecase.AddDiscussionUseCase
import com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository.DiscussionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AddDiscussionModule {

    @Provides
    @Singleton
    fun provideAddDiscussionUseCase(
        discussionRepository: DiscussionRepository
    ): AddDiscussionUseCase {
        return AddDiscussionUseCase(discussionRepository)
    }
}

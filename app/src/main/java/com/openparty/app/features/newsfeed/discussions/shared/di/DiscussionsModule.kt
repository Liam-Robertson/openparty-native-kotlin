package com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.di

import com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository.DiscussionRepository
import com.openparty.app.features.newsfeed.discussions.shared.data.repository.DiscussionRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DiscussionsModule {

    @Binds
    @Singleton
    abstract fun bindDiscussionRepository(
        implementation: DiscussionRepositoryImpl
    ): DiscussionRepository
}

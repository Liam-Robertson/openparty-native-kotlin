package com.openparty.app.features.engagement.comments.feature_comments_section.di

import com.google.firebase.firestore.FirebaseFirestore
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.usecase.GetCommentsUseCase
import com.openparty.app.features.engagement.comments.shared.data.datasource.CommentsDataSource
import com.openparty.app.features.engagement.comments.shared.data.datasource.FirebaseCommentsDataSource
import com.openparty.app.features.engagement.comments.shared.data.repository.CommentsRepositoryImpl
import com.openparty.app.features.engagement.comments.shared.domain.repository.CommentsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent

@Module
@InstallIn(ViewModelComponent::class)
object CommentsSectionModule {

    @Provides
    fun provideCommentsDataSource(
        firestore: FirebaseFirestore
    ): CommentsDataSource {
        return FirebaseCommentsDataSource(firestore)
    }

    @Provides
    fun provideCommentsRepository(
        dataSource: CommentsDataSource
    ): CommentsRepository {
        return CommentsRepositoryImpl(dataSource)
    }

    @Provides
    fun provideGetCommentsUseCase(
        repository: CommentsRepository
    ): GetCommentsUseCase {
        return GetCommentsUseCase(repository)
    }
}

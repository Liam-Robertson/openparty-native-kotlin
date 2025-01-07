package com.openparty.app.features.newsfeed.discussions.feature_add_discussion.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository.DiscussionRepository
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import timber.log.Timber
import javax.inject.Inject

class AddDiscussionUseCase @Inject constructor(
    private val discussionRepository: DiscussionRepository
) {
    suspend operator fun invoke(discussion: Discussion): DomainResult<Discussion> {
        Timber.d("AddDiscussionUseCase invoked with discussion: ${discussion.title}")

        return try {
            when (val result = discussionRepository.addDiscussion(discussion)) {
                is DomainResult.Success -> {
                    Timber.d("Successfully added discussion: ${discussion.title}")
                    DomainResult.Success(result.data)
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to add discussion: ${discussion.title}, Error: ${result.error}")
                    DomainResult.Failure(result.error)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error while adding discussion: ${discussion.title}")
            DomainResult.Failure(AppError.Discussion.AddDiscussion)
        }
    }
}

package com.openparty.app.features.newsfeed.discussions.feature_discussions_article.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import com.openparty.app.features.newsfeed.discussions.shared.data.domain.repository.DiscussionRepository
import timber.log.Timber
import javax.inject.Inject

class GetDiscussionByIdUseCase @Inject constructor(
    private val discussionRepository: DiscussionRepository
) {
    suspend operator fun invoke(discussionId: String): DomainResult<Discussion> {
        Timber.d("Fetching discussion with ID: %s", discussionId)

        return try {
            val result = discussionRepository.getDiscussionById(discussionId)
            when (result) {
                is DomainResult.Success -> {
                    Timber.d("Successfully fetched discussion: %s", result.data)
                    DomainResult.Success(result.data)
                }
                is DomainResult.Failure -> {
                    Timber.e("Failed to fetch discussion with ID: %s, returning FetchDiscussions error", discussionId)
                    DomainResult.Failure(AppError.Discussion.FetchDiscussions)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Exception occurred while fetching discussion with ID: %s", discussionId)
            DomainResult.Failure(AppError.Discussion.FetchDiscussions)
        }
    }
}

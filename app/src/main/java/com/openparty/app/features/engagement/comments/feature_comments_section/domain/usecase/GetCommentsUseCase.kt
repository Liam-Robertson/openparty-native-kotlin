package com.openparty.app.features.engagement.comments.feature_comments_section.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.CommentFetchCriteria
import com.openparty.app.features.engagement.comments.shared.domain.repository.CommentsRepository
import timber.log.Timber
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val repository: CommentsRepository
) {
    suspend operator fun invoke(criteria: CommentFetchCriteria): DomainResult<List<Comment>> {
        Timber.d("GetCommentsUseCase invoked with criteria: $criteria")
        val commentsResult = try {
            when (criteria) {
                is CommentFetchCriteria.ForDiscussion -> {
                    Timber.d("Fetching comments for discussionId: ${criteria.discussionId}")
                    repository.getComments(criteria.discussionId, null)
                }
                is CommentFetchCriteria.ForCouncilMeeting -> {
                    Timber.d("Fetching comments for councilMeetingId: ${criteria.councilMeetingId}")
                    repository.getComments(null, criteria.councilMeetingId)
                }

            }
        } catch (e: Exception) {
            Timber.e(e, "Error while executing GetCommentsUseCase with criteria: $criteria")
            DomainResult.Failure(AppError.Comments.FetchComments)
        }

        return when (commentsResult) {
            is DomainResult.Success -> {
                val sortedComments = commentsResult.data.sortedByDescending { it.upvoteCount }
                Timber.d("Successfully fetched and sorted ${sortedComments.size} comments.")
                DomainResult.Success(sortedComments)
            }
            is DomainResult.Failure -> {
                Timber.e("Failed to fetch comments for criteria: $criteria")
                DomainResult.Failure(AppError.Comments.FetchComments)
            }
        }
    }
}

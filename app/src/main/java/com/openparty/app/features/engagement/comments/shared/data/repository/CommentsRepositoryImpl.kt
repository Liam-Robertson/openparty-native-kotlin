package com.openparty.app.features.engagement.comments.shared.data.repository

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment
import com.openparty.app.features.engagement.comments.shared.data.datasource.CommentsDataSource
import com.openparty.app.features.engagement.comments.shared.domain.repository.CommentsRepository
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentsRepositoryImpl @Inject constructor(
    private val commentsDataSource: CommentsDataSource
) : CommentsRepository {

    override suspend fun getComments(
        discussionId: String?,
        councilMeetingId: String?
    ): DomainResult<List<Comment>> {
        Timber.d("Fetching comments with discussionId: $discussionId, councilMeetingId: $councilMeetingId")
        return try {
            val comments = when {
                discussionId != null -> commentsDataSource.getCommentsForDiscussion(discussionId)
                councilMeetingId != null -> commentsDataSource.getCommentsForCouncilMeeting(councilMeetingId)
                else -> throw IllegalArgumentException("Either discussionId or councilMeetingId must be provided.")
            }
            Timber.d("Successfully fetched ${comments.size} comments.")
            DomainResult.Success(comments)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching comments.")
            DomainResult.Failure(AppError.Comments.General)
        }
    }

    override suspend fun addComment(comment: Comment) {
        Timber.d("Adding comment: $comment")
        try {
            commentsDataSource.addComment(comment)
            Timber.d("Successfully added comment with ID: ${comment.commentId}")
        } catch (e: Exception) {
            Timber.e(e, "Error adding comment: $comment")
            DomainResult.Failure(AppError.Comments.General)
        }
    }
}

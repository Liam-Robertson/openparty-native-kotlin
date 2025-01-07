package com.openparty.app.features.engagement.comments.feature_add_comment.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment
import com.openparty.app.features.engagement.comments.shared.domain.repository.CommentsRepository
import timber.log.Timber
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val repository: CommentsRepository
) {
    suspend operator fun invoke(comment: Comment): DomainResult<Unit> {
        Timber.d("AddCommentUseCase invoked with comment: $comment")
        return try {
            repository.addComment(comment)
            Timber.d("Successfully added comment with ID: ${comment.commentId}")
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error occurred while adding comment: $comment")
            DomainResult.Failure(AppError.Comments.AddComment)
        }
    }
}

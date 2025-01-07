package com.openparty.app.features.engagement.comments.shared.domain.repository

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment

interface CommentsRepository {
    suspend fun getComments(
        discussionId: String? = null,
        councilMeetingId: String? = null
    ): DomainResult<List<Comment>>

    suspend fun addComment(comment: Comment)
}

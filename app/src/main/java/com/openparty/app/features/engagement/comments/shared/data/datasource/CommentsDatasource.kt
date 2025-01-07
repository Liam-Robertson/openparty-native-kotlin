package com.openparty.app.features.engagement.comments.shared.data.datasource

import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment


interface CommentsDataSource {
    suspend fun getCommentsForDiscussion(discussionId: String): List<Comment>
    suspend fun getCommentsForCouncilMeeting(councilMeetingId: String): List<Comment>
    suspend fun addComment(comment: Comment)
}

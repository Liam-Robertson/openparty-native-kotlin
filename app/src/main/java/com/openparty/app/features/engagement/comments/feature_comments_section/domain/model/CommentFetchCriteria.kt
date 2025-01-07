package com.openparty.app.features.engagement.comments.feature_comments_section.domain.model

sealed class CommentFetchCriteria {
    data class ForDiscussion(val discussionId: String) : CommentFetchCriteria()
    data class ForCouncilMeeting(val councilMeetingId: String) : CommentFetchCriteria()
}

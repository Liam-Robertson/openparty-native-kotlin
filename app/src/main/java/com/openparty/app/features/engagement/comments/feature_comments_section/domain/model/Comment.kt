package com.openparty.app.features.engagement.comments.feature_comments_section.domain.model

import java.util.Date

data class Comment(
    var commentId: String = "",
    var contentText: String = "",
    var councilMeetingId: Int? = null,
    var discussionId: String? = null,
    var downvoteCount: Int = 0,
    var upvoteCount: Int = 0,
    var parentCommentId: String? = null,
    var screenName: String = "",
    val timestamp: Date? = null,
    var userId: String = ""
)

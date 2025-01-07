package com.openparty.app.features.newsfeed.discussions.shared.domain.model

import java.util.Date

data class Discussion(
    val discussionId: String = "",
    val title: String = "",
    val contentText: String = "",
    val timestamp: Date? = null,
    val commentCount: Int = 0,
    val upvoteCount: Int = 0,
    val downvoteCount: Int = 0
)

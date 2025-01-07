package com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_article.presentation.components

import com.openparty.app.features.newsfeed.council_meetings.shared.domain.model.CouncilMeeting


data class CouncilMeetingArticleUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val councilMeeting: CouncilMeeting? = null,
    val isPlaying: Boolean = false
)

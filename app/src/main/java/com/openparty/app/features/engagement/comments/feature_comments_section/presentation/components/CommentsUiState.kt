package com.openparty.app.features.engagement.comments.feature_comments_section.presentation.components

import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment


data class CommentsUiState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
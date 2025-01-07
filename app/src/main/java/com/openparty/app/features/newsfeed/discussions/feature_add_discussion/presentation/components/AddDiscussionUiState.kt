package com.openparty.app.features.newsfeed.discussions.feature_add_discussion.presentation.components

import androidx.compose.ui.text.input.TextFieldValue

data class AddDiscussionUiState(
    val title: TextFieldValue = TextFieldValue(""),
    val contentText: TextFieldValue = TextFieldValue(""),
    val errorMessage: String = "",
    val isLoading: Boolean = false
)

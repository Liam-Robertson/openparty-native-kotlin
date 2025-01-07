package com.openparty.app.features.newsfeed.discussions.feature_add_discussion.presentation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.openparty.app.core.analytics.domain.usecase.TrackDiscussionPostedUseCase
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.newsfeed.discussions.feature_add_discussion.domain.usecase.AddDiscussionUseCase
import com.openparty.app.features.newsfeed.discussions.feature_add_discussion.presentation.components.AddDiscussionUiState
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import com.openparty.app.navigation.NavDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AddDiscussionViewModel @Inject constructor(
    private val addDiscussionUseCase: AddDiscussionUseCase,
    private val trackDiscussionPostedUseCase: TrackDiscussionPostedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddDiscussionUiState())
    val uiState: StateFlow<AddDiscussionUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    fun onTitleTextChanged(newText: TextFieldValue) {
        updateUiState(title = newText)
    }

    fun onContentTextChanged(newText: TextFieldValue) {
        updateUiState(contentText = newText)
    }

    fun onBackClicked() {
        emitUiEvent(UiEvent.Navigate(NavDestinations.Back))
    }

    fun onPostClicked() {
        val currentState = _uiState.value
        if (isInputValid(currentState)) {
            val discussion = createDiscussionFromState(currentState)
            postDiscussion(discussion)
        } else {
            updateUiState(errorMessage = "Title and content cannot be empty")
        }
    }

    private fun createDiscussionFromState(state: AddDiscussionUiState): Discussion {
        return Discussion(
            discussionId = "",
            title = state.title.text,
            contentText = state.contentText.text,
            timestamp = Timestamp.now().toDate(),
            upvoteCount = 0,
            downvoteCount = 0,
            commentCount = 0
        )
    }

    private fun postDiscussion(discussion: Discussion) {
        viewModelScope.launch {
            updateUiState(isLoading = true)
            when (val result = addDiscussionUseCase(discussion)) {
                is DomainResult.Success -> {
                    val addedDiscussion = result.data
                    val discussionId = addedDiscussion.discussionId
                    val title = addedDiscussion.title

                    trackDiscussionPosted(discussionId, title)
                    emitUiEvent(UiEvent.Navigate(NavDestinations.DiscussionsPreview))
                    resetUiState()
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    updateUiState(isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }

    private suspend fun trackDiscussionPosted(discussionId: String, title: String) {
        when (val result = trackDiscussionPostedUseCase(discussionId, title)) {
            is DomainResult.Success -> Timber.i("Discussion posted event tracked: $discussionId")
            is DomainResult.Failure -> Timber.e("Failed to track discussion posted event for ID: $discussionId")
        }
    }

    private fun updateUiState(
        title: TextFieldValue? = null,
        contentText: TextFieldValue? = null,
        isLoading: Boolean? = null,
        errorMessage: String? = null
    ) {
        val currentState = _uiState.value
        _uiState.value = currentState.copy(
            title = title ?: currentState.title,
            contentText = contentText ?: currentState.contentText,
            isLoading = isLoading ?: currentState.isLoading,
            errorMessage = errorMessage ?: currentState.errorMessage
        )
    }

    private fun resetUiState() {
        _uiState.value = AddDiscussionUiState()
    }

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }

    private fun isInputValid(state: AddDiscussionUiState): Boolean {
        return state.title.text.isNotBlank() && state.contentText.text.isNotBlank()
    }
}

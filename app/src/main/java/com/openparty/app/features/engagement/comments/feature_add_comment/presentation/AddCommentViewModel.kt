package com.openparty.app.features.engagement.comments.feature_add_comment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.engagement.comments.feature_add_comment.domain.usecase.AddCommentUseCase
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment
import com.openparty.app.features.shared.feature_user.domain.usecase.GetUserUseCase
import com.openparty.app.navigation.NavDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import com.openparty.app.core.analytics.domain.usecase.TrackCommentPostedUseCase
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.features.engagement.comments.feature_add_comment.presentation.components.AddCommentUiState
import com.openparty.app.features.shared.feature_user.domain.model.User
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class AddCommentViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val trackCommentPostedUseCase: TrackCommentPostedUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    val discussionId: String = savedStateHandle["discussionId"] ?: ""
    val titleText: String = savedStateHandle["titleText"] ?: ""

    private val _uiState = MutableStateFlow(AddCommentUiState())
    val uiState: StateFlow<AddCommentUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    fun onCommentTextChanged(newText: TextFieldValue) {
        _uiState.value = _uiState.value.copy(commentText = newText)
    }

    fun onBackClicked() {
        emitUiEvent(UiEvent.Navigate(NavDestinations.Back))
    }

    fun onPostClicked() {
        val currentState = _uiState.value
        if (currentState.commentText.text.isBlank()) {
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Comment cannot be empty")
            return
        }
        viewModelScope.launch {
            _uiState.value = currentState.copy(isLoading = true)
            when (val userResult = getUserUseCase()) {
                is DomainResult.Success -> handleSuccessfulUserFetch(currentState, userResult.data)
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(userResult.error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }

    private fun handleSuccessfulUserFetch(currentState: AddCommentUiState, user: User) {
        val commentData = createCommentData(currentState, user)
        viewModelScope.launch {
            when (val result = addCommentUseCase(commentData)) {
                is DomainResult.Success -> {
                    trackCommentPostedUseCase(commentData.commentId, discussionId, currentState.commentText.text)
                    emitUiEvent(UiEvent.Navigate(NavDestinations.Back))
                    resetUiState()
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }

    private fun createCommentData(currentState: AddCommentUiState, user: User): Comment {
        return Comment(
            commentId = "",
            userId = user.userId,
            screenName = user.screenName,
            contentText = currentState.commentText.text,
            timestamp = Timestamp.now().toDate(),
            upvoteCount = 0,
            downvoteCount = 0,
            parentCommentId = null,
            discussionId = discussionId,
            councilMeetingId = null
        )
    }

    private fun resetUiState() {
        _uiState.value = AddCommentUiState()
    }

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _uiEvent.emit(event)
        }
    }
}

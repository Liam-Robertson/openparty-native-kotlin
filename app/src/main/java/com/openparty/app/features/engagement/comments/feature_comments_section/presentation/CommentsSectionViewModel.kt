package com.openparty.app.features.engagement.comments.feature_comments_section.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.Comment
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.model.CommentFetchCriteria
import com.openparty.app.features.engagement.comments.feature_comments_section.domain.usecase.GetCommentsUseCase
import com.openparty.app.features.engagement.comments.feature_comments_section.presentation.components.CommentsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class CommentsSectionViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CommentsUiState())
    val uiState: StateFlow<CommentsUiState> = _uiState

    private val discussionId: String? = savedStateHandle["discussionId"]
    private val councilMeetingId: String? = savedStateHandle["councilMeetingId"]

    init {
        loadComments()
    }

    private fun loadComments() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val getCommentsResult = when {
                discussionId != null -> getCommentsUseCase(CommentFetchCriteria.ForDiscussion(discussionId))
                councilMeetingId != null -> getCommentsUseCase(CommentFetchCriteria.ForCouncilMeeting(councilMeetingId))
                else -> {
                    _uiState.value = _uiState.value.copy(
                        comments = emptyList(),
                        isLoading = false,
                        errorMessage = "No valid discussion or council meeting ID provided"
                    )
                    return@launch
                }
            }

            _uiState.value = when (getCommentsResult) {
                is DomainResult.Success -> {
                    _uiState.value.copy(comments = getCommentsResult.data, isLoading = false, errorMessage = null)
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(getCommentsResult.error)
                    Timber.e(getCommentsResult.error, "Error loading discussions")
                    _uiState.value.copy(comments = emptyList(), isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }

    fun formatTimeDiff(date: Date?): String {
        if (date == null) return "Unknown time"
        val now = System.currentTimeMillis()
        val timestamp = date.time
        val diff = now - timestamp
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)
        val years = days / 365

        return when {
            years >= 1 -> "$years years ago"
            days >= 1 -> "$days days ago"
            hours >= 1 -> "$hours hours ago"
            minutes >= 1 -> "$minutes minutes ago"
            else -> "Just now"
        }
    }
}

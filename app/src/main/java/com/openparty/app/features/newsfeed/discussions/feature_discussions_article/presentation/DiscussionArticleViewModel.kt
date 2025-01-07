package com.openparty.app.features.newsfeed.discussions.feature_discussions_article.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.core.shared.presentation.UiState
import com.openparty.app.features.newsfeed.discussions.feature_discussions_article.domain.usecase.GetDiscussionByIdUseCase
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscussionArticleViewModel @Inject constructor(
    private val getDiscussionByIdUseCase: GetDiscussionByIdUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _discussion = MutableStateFlow<Discussion?>(null)
    val discussion: StateFlow<Discussion?> = _discussion

    private val discussionId: String? = savedStateHandle["discussionId"]

    init {
        loadDiscussion()
    }

    private fun loadDiscussion() {
        if (discussionId == null) {
            val errorMessage = AppErrorMapper.getUserFriendlyMessage(AppError.Discussion.FetchDiscussions)
            _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getDiscussionByIdUseCase(discussionId)) {
                is DomainResult.Success -> {
                    _discussion.value = result.data
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }
}

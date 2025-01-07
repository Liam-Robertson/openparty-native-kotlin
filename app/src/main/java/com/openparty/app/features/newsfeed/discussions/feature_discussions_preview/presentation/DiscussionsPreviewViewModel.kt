package com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.openparty.app.core.analytics.domain.usecase.TrackDiscussionSelectedUseCase
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.core.shared.presentation.UiState
import com.openparty.app.features.newsfeed.discussions.shared.domain.model.Discussion
import com.openparty.app.features.newsfeed.discussions.feature_discussions_preview.domain.usecase.GetDiscussionsUseCase
import com.openparty.app.navigation.NavDestinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class DiscussionsPreviewViewModel @Inject constructor(
    private val getDiscussionsUseCase: GetDiscussionsUseCase,
    private val trackDiscussionSelectedUseCase: TrackDiscussionSelectedUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    private var _discussions: Flow<PagingData<Discussion>> = flow {}
    val discussions: Flow<PagingData<Discussion>>
        get() = _discussions

    init {
        loadDiscussions()
    }

    private fun loadDiscussions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val result = getDiscussionsUseCase()) {
                is DomainResult.Success -> {
                    _discussions = result.data.cachedIn(viewModelScope)
                }
                is DomainResult.Failure -> {
                    val errorMessage = AppErrorMapper.getUserFriendlyMessage(result.error)
                    Timber.e(result.error, "Error loading discussions")
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = errorMessage)
                }
            }
        }
    }

    fun onDiscussionSelected(discussionId: String) {
        viewModelScope.launch {
            when (val result = trackDiscussionSelectedUseCase(discussionId)) {
                is DomainResult.Success -> Timber.i("Discussion selected event tracked: $discussionId")
                is DomainResult.Failure -> Timber.e("Failed to track discussion selected event for ID: $discussionId")
            }

            _uiEvent.emit(UiEvent.Navigate(NavDestinations.DiscussionsArticle(discussionId)))
        }
    }
}

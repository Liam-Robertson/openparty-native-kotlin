package com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_article.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.core.shared.domain.error.AppErrorMapper
import com.openparty.app.core.firebase.feature_firebase_storage.domain.usecase.ResolveUrlUseCase
import com.openparty.app.core.shared.presentation.UiEvent
import com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_article.domain.usecase.GetCouncilMeetingByIdUseCase
import com.openparty.app.features.newsfeed.council_meetings.feature_council_meetings_article.presentation.components.CouncilMeetingArticleUiState
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.usecase.PauseAudioUseCase
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.usecase.PlayAudioUseCase
import com.openparty.app.features.newsfeed.council_meetings.shared.domain.model.CouncilMeeting
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CouncilMeetingArticleViewModel @Inject constructor(
    private val getCouncilMeetingByIdUseCase: GetCouncilMeetingByIdUseCase,
    private val resolveUrlUseCase: ResolveUrlUseCase,
    private val playAudioUseCase: PlayAudioUseCase,
    private val pauseAudioUseCase: PauseAudioUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CouncilMeetingArticleUiState())
    val uiState: StateFlow<CouncilMeetingArticleUiState> = _uiState

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent

    val councilMeetingId: String? = savedStateHandle["councilMeetingId"]

    init {
        initializeCouncilMeeting()
    }

    private fun initializeCouncilMeeting() {
        val id = councilMeetingId
        if (id == null) {
            handleError(AppError.CouncilMeeting.FetchCouncilMeetings)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            fetchCouncilMeeting(id)
        }
    }

    private suspend fun fetchCouncilMeeting(id: String) {
        when (val result = getCouncilMeetingByIdUseCase(id)) {
            is DomainResult.Success -> resolveAudioUrl(result.data)
            is DomainResult.Failure -> handleError(result.error, isLoading = false)
        }
    }

    private suspend fun resolveAudioUrl(meeting: CouncilMeeting) {
        when (val resolvedResult = resolveUrlUseCase(meeting.audioUrl)) {
            is DomainResult.Success -> {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    councilMeeting = meeting.copy(audioUrl = resolvedResult.data)
                )
            }
            is DomainResult.Failure -> handleError(resolvedResult.error, isLoading = false)
        }
    }

    fun togglePlayback() {
        viewModelScope.launch {
            val audioUrl = _uiState.value.councilMeeting?.audioUrl.orEmpty()
            if (audioUrl.isEmpty()) {
                handleError(AppError.CouncilMeeting.PlayAudio)
                return@launch
            }
            if (_uiState.value.isPlaying) {
                handlePause()
            } else {
                handlePlay(audioUrl)
            }
        }
    }

    private fun handlePlay(audioUrl: String) {
        when (val result = playAudioUseCase.execute(audioUrl)) {
            is DomainResult.Success -> {
                _uiState.value = _uiState.value.copy(isPlaying = true)
            }
            is DomainResult.Failure -> {
                handleError(result.error)
            }
        }
    }

    private fun handlePause() {
        when (val result = pauseAudioUseCase.execute()) {
            is DomainResult.Success -> {
                _uiState.value = _uiState.value.copy(isPlaying = false)
            }
            is DomainResult.Failure -> {
                handleError(result.error)
            }
        }
    }

    private fun handleError(error: AppError, isLoading: Boolean = false) {
        val errorMessage = AppErrorMapper.getUserFriendlyMessage(error)
        _uiState.value = _uiState.value.copy(
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }
}

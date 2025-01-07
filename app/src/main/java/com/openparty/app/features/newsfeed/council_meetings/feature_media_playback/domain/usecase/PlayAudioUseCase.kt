package com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.PlaybackManager
import timber.log.Timber
import javax.inject.Inject

class PlayAudioUseCase @Inject constructor(
    private val playbackManager: PlaybackManager
) {
    fun execute(audioUrl: String): DomainResult<Unit> {
        return try {
            Timber.d("Executing PlayAudioUseCase with URL: $audioUrl")
            if (audioUrl.isBlank()) {
                Timber.e("Audio URL is blank")
                DomainResult.Failure(AppError.CouncilMeeting.PlayAudio)
            } else {
                playbackManager.playAudio(audioUrl)
                Timber.d("Playback started successfully for URL: $audioUrl")
                DomainResult.Success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in PlayAudioUseCase")
            DomainResult.Failure(AppError.CouncilMeeting.PlayAudio)
        }
    }
}

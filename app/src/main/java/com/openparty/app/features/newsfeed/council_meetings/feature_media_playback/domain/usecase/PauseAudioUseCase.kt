package com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.usecase

import com.openparty.app.core.shared.domain.DomainResult
import com.openparty.app.core.shared.domain.error.AppError
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain.PlaybackManager
import timber.log.Timber
import javax.inject.Inject

class PauseAudioUseCase @Inject constructor(
    private val playbackManager: PlaybackManager
) {
    fun execute(): DomainResult<Unit> {
        return try {
            Timber.d("Executing PauseAudioUseCase")
            playbackManager.pauseAudio()
            Timber.d("Playback paused successfully")
            DomainResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error in PauseAudioUseCase")
            DomainResult.Failure(AppError.CouncilMeeting.PauseAudio)
        }
    }
}

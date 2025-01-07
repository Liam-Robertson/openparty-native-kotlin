package com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.domain

import android.content.Context
import com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.data.MediaPlaybackService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaybackManager @Inject constructor(
    private val context: Context
) {
    fun playAudio(audioUrl: String) {
        MediaPlaybackService.playAudio(context, audioUrl)
    }

    fun pauseAudio() {
        MediaPlaybackService.pauseAudio(context)
    }
}

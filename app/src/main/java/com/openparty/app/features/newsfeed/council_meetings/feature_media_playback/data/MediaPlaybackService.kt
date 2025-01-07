package com.openparty.app.features.newsfeed.council_meetings.feature_media_playback.data

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.media.app.NotificationCompat.MediaStyle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.openparty.app.main.MainActivity
import timber.log.Timber

class MediaPlaybackService : MediaSessionService() {

    private var exoPlayer: ExoPlayer? = null
    private var mediaSession: MediaSession? = null

    companion object {
        const val ACTION_PLAY = "ACTION_PLAY"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_PREVIOUS = "ACTION_PREVIOUS"
        const val ACTION_NEXT = "ACTION_NEXT"
        const val EXTRA_AUDIO_URL = "EXTRA_AUDIO_URL"
        const val CHANNEL_ID = "MEDIA_PLAYBACK_CHANNEL"
        const val NOTIFICATION_ID = 1001

        fun playAudio(context: Context, audioUrl: String) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_AUDIO_URL, audioUrl)
            }
            context.startForegroundService(intent)
        }

        fun pauseAudio(context: Context) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_PAUSE
            }
            context.startForegroundService(intent)
        }

        fun previousAudio(context: Context) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_PREVIOUS
            }
            context.startForegroundService(intent)
        }

        fun nextAudio(context: Context) {
            val intent = Intent(context, MediaPlaybackService::class.java).apply {
                action = ACTION_NEXT
            }
            context.startForegroundService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            createNotificationChannel()
            exoPlayer = ExoPlayer.Builder(this).build().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(C.USAGE_MEDIA)
                        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                        .build(),
                    true
                )
            }
            val activityIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            mediaSession = MediaSession.Builder(this, exoPlayer!!)
                .setSessionActivity(pendingIntent)
                .setCallback(object : MediaSession.Callback {})
                .build()
            startForeground(NOTIFICATION_ID, buildNotification())
            Timber.d("Service created and notification started")
        } catch (e: Exception) {
            Timber.e(e, "Error in onCreate")
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        try {
            when (intent?.action) {
                ACTION_PLAY -> {
                    val url = intent.getStringExtra(EXTRA_AUDIO_URL).orEmpty()
                    if (url.isNotEmpty()) {
                        val mediaItem = MediaItem.Builder().setUri(url).build()
                        exoPlayer?.setMediaItem(mediaItem)
                        exoPlayer?.prepare()
                        exoPlayer?.play()
                        Timber.d("Playing audio: $url")
                    } else {
                        Timber.e("Audio URL is empty")
                    }
                }
                ACTION_PAUSE -> {
                    exoPlayer?.pause()
                    Timber.d("Audio paused")
                }
                ACTION_PREVIOUS -> {
                    Timber.d("Previous track action")
                }
                ACTION_NEXT -> {
                    Timber.d("Next track action")
                }
                else -> {
                    Timber.w("Unknown action: ${intent?.action}")
                }
            }
            updateNotification()
        } catch (e: Exception) {
            Timber.e(e, "Error in onStartCommand")
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            mediaSession?.release()
            exoPlayer?.release()
            Timber.d("Service destroyed and resources released")
        } catch (e: Exception) {
            Timber.e(e, "Error in onDestroy")
        }
        super.onDestroy()
    }

    private fun updateNotification() {
        try {
            val notification = buildNotification()
            startForeground(NOTIFICATION_ID, notification)
            Timber.d("Notification updated")
        } catch (e: Exception) {
            Timber.e(e, "Error updating notification")
        }
    }

    @OptIn(UnstableApi::class)
    private fun buildNotification(): Notification {
        return try {
            val isPlaying = exoPlayer?.isPlaying == true
            val actionIcon = if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            val actionName = if (isPlaying) "Pause" else "Play"
            val actionIntent = if (isPlaying) getPendingIntent(ACTION_PAUSE) else getPendingIntent(ACTION_PLAY)

            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Playing Audio")
                .setContentText("Foreground Audio Playback")
                .setOngoing(true)
                .setContentIntent(mediaSession?.sessionActivity)
                .setStyle(
                    MediaStyle()
                        .setMediaSession(mediaSession?.sessionCompatToken)
                        .setShowActionsInCompactView(0, 1, 2)
                )
                .addAction(NotificationCompat.Action(
                    android.R.drawable.ic_media_previous, "Previous", getPendingIntent(ACTION_PREVIOUS)))
                .addAction(NotificationCompat.Action(actionIcon, actionName, actionIntent))
                .addAction(NotificationCompat.Action(
                    android.R.drawable.ic_media_next, "Next", getPendingIntent(ACTION_NEXT)))
                .build()
        } catch (e: Exception) {
            Timber.e(e, "Error building notification")
            throw e
        }
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, MediaPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

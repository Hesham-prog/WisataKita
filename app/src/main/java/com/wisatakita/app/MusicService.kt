package com.wisatakita.app

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PAUSE -> {
                mediaPlayer?.pause()
                isPlaying = false
            }
            ACTION_RESUME -> {
                mediaPlayer?.start()
                isPlaying = true
            }
            else -> {
                if (mediaPlayer == null) {
                    val resId = resources.getIdentifier("background_music", "raw", packageName)
                    if (resId != 0) {
                        mediaPlayer = MediaPlayer.create(this, resId)
                        mediaPlayer?.isLooping = true
                        mediaPlayer?.setVolume(0.4f, 0.4f)
                        mediaPlayer?.start()
                        isPlaying = true
                    }
                    isRunning = true
                }
            }
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isRunning = false
        isPlaying = true
    }

    companion object {
        const val ACTION_PAUSE = "PAUSE"
        const val ACTION_RESUME = "RESUME"
        var isRunning = false
        var isPlaying = true
    }
}

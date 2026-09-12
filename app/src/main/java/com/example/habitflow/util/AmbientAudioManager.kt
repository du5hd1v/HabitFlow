package com.example.habitflow.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.habitflow.R

class AmbientAudioManager {
    private var mediaPlayer: MediaPlayer? = null
    private var currentPreset: String? = null
    private var isPlayingState = false
    private val lock = Any()

    companion object {
        private const val TAG = "AmbientAudioManager"
    }

    fun play(preset: String) {
        synchronized(lock) {
            if (isPlayingState && currentPreset == preset) {
                stop()
                return
            }
            stop()
            currentPreset = preset
            isPlayingState = true
        }
    }

    fun play(context: Context, preset: String) {
        synchronized(lock) {
            if (isPlayingState && currentPreset == preset) {
                stop()
                return
            }
            // 1. Safe MediaPlayer Handling: Stop and release any existing instance
            stop()

            currentPreset = preset
            isPlayingState = true

            try {
                val mp = when (preset) {
                    "Rain" -> MediaPlayer.create(context, R.raw.rain)
                    "Lo-Fi Beats" -> MediaPlayer.create(context, R.raw.lofi)
                    "Cafe" -> MediaPlayer.create(context, R.raw.cafe)
                    else -> MediaPlayer.create(context, R.raw.rain)
                }

                // 2. Robust Initialization: Check if MediaPlayer.create returned null
                if (mp == null) {
                    Log.e(TAG, "MediaPlayer.create returned null for preset: $preset")
                    isPlayingState = false
                    currentPreset = null
                    mediaPlayer = null
                    return
                }

                mp.apply {
                    isLooping = true
                    setVolume(1.0f, 1.0f)
                    start()
                }
                mediaPlayer = mp
            } catch (e: Exception) {
                Log.e(TAG, "Exception while initializing or starting MediaPlayer for preset: $preset", e)
                mediaPlayer?.release()
                mediaPlayer = null
                isPlayingState = false
                currentPreset = null
            }
        }
    }

    fun stop() {
        synchronized(lock) {
            isPlayingState = false
            currentPreset = null
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            } catch (e: Exception) {
                Log.e(TAG, "Exception while stopping/releasing MediaPlayer", e)
            } finally {
                mediaPlayer = null
            }
        }
    }

    // 3. Lifecycle Cleanup: Complete resource cleanup
    fun release() {
        stop()
    }

    fun isPlaying(): Boolean = synchronized(lock) { isPlayingState }
    fun getCurrentPreset(): String? = synchronized(lock) { currentPreset }
}

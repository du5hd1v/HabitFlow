package com.example.habitflow.data.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager

import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.habitflow.MainActivity
import com.example.habitflow.R
import kotlinx.coroutines.*
import java.util.Locale

class TimerForegroundService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private var timerJob: Job? = null
    private var totalSeconds = 25 * 60
    private var remainingSeconds = totalSeconds
    private var isRunning = false

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val ACTION_RESET = "ACTION_RESET"

        const val EXTRA_SECONDS = "EXTRA_SECONDS"
        const val BROADCAST_TIMER_TICK = "com.example.habitflow.TIMER_TICK"
        const val BROADCAST_TIMER_COMPLETE = "com.example.habitflow.TIMER_COMPLETE"

        const val NOTIFICATION_ID = 1001
        const val CHANNEL_ID = "pomodoro_channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val customSeconds = intent.getIntExtra(EXTRA_SECONDS, -1)
                if (customSeconds > 0) {
                    totalSeconds = customSeconds
                    remainingSeconds = customSeconds
                }
                startTimer()
            }
            ACTION_PAUSE -> pauseTimer()
            ACTION_STOP -> stopTimerService()
            ACTION_RESET -> {
                pauseTimer()
                remainingSeconds = totalSeconds
                updateNotification("Timer Reset: ${formatTime(remainingSeconds)}")
                broadcastTick()
            }
        }
        return START_STICKY
    }

    private fun startTimer() {
        if (isRunning) return
        isRunning = true
        startForeground(NOTIFICATION_ID, createNotification("Pomodoro Active: ${formatTime(remainingSeconds)}"))

        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && remainingSeconds > 0 && isRunning) {
                delay(1000L)
                remainingSeconds--
                updateNotification("Focus Time: ${formatTime(remainingSeconds)}")
                broadcastTick()

                if (remainingSeconds <= 0) {
                    isRunning = false
                    broadcastComplete()
                    stopSelf()
                    break
                }
            }
        }
    }

    private fun pauseTimer() {
        isRunning = false
        timerJob?.cancel()
        updateNotification("Timer Paused: ${formatTime(remainingSeconds)}")
        broadcastTick()
    }

    private fun stopTimerService() {
        isRunning = false
        timerJob?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun formatTime(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pomodoro Timer Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps Pomodoro timer running in background"
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(contentText: String): android.app.Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val pauseIntent = Intent(this, TimerForegroundService::class.java).apply { action = ACTION_PAUSE }
        val pausePendingIntent = PendingIntent.getService(this, 1, pauseIntent, PendingIntent.FLAG_IMMUTABLE)

        val stopIntent = Intent(this, TimerForegroundService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 2, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HabitFlow Pomodoro")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.notify(NOTIFICATION_ID, createNotification(contentText))
    }

    private fun broadcastTick() {
        val intent = Intent(BROADCAST_TIMER_TICK).apply {
            putExtra("remaining_seconds", remainingSeconds)
            putExtra("is_running", isRunning)
        }
        sendBroadcast(intent)
    }

    private fun broadcastComplete() {
        val intent = Intent(BROADCAST_TIMER_COMPLETE)
        sendBroadcast(intent)
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }
}

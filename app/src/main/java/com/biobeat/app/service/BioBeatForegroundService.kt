package com.biobeat.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.biobeat.app.MainActivity
import com.biobeat.app.R
import com.biobeat.app.domain.repository.DeviceRepository
import com.biobeat.sdk.connection.ConnectionState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BioBeatForegroundService : Service() {

    @Inject
    lateinit var deviceRepository: DeviceRepository

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        const val CHANNEL_ID = "biobeat_monitoring"
        const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, BioBeatForegroundService::class.java)
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, BioBeatForegroundService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Monitoring..."))
        observeHeartRate()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun observeHeartRate() {
        scope.launch {
            deviceRepository.heartRate.collect { reading ->
                val notification = buildNotification("Heart Rate: ${reading.bpm} BPM")
                val nm = getSystemService(NotificationManager::class.java)
                nm.notify(NOTIFICATION_ID, notification)
            }
        }

        scope.launch {
            deviceRepository.connectionState.collect { state ->
                if (state is ConnectionState.Disconnected || state is ConnectionState.Failed) {
                    stopSelf()
                }
            }
        }
    }

    private fun buildNotification(content: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BioBeat")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "BioBeat Monitoring",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows live heart rate while monitoring"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}

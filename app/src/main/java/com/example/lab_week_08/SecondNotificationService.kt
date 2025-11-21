package com.example.lab_week_08

import android.app.*
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat

class SecondNotificationService : Service() {

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        notificationBuilder = startForegroundServiceCustom()

        val thread = HandlerThread("SecondNotificationThread").apply { start() }
        serviceHandler = Handler(thread.looper)
    }

    private fun startForegroundServiceCustom(): NotificationCompat.Builder {

        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = createNotificationChannel()

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Second Notification Service Running")
            .setContentText("Starting...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)

        startForeground(2001, builder.build())
        return builder
    }

    private fun createNotificationChannel(): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "second_channel",
                "Second Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = ContextCompat.getSystemService(
                this, NotificationManager::class.java
            )!!
            manager.createNotificationChannel(channel)
            return "second_channel"
        }
        return ""
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        serviceHandler.post {
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            for (i in 5 downTo 0) {
                Thread.sleep(1000)
                notificationBuilder.setContentText("$i seconds remaining...")
                notificationManager.notify(2001, notificationBuilder.build())
            }

            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }

        return super.onStartCommand(intent, flags, startId)
    }
}

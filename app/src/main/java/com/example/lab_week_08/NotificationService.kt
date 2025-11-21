package com.example.lab_week_08

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class NotificationService : Service() {

    // Notification builder
    private lateinit var notificationBuilder: NotificationCompat.Builder

    // Handler for background thread
    private lateinit var serviceHandler: Handler

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        // Build & start the foreground notification
        notificationBuilder = startForegroundService()

        // Create a HandlerThread for background tasks
        val handlerThread = HandlerThread("SecondThread").apply { start() }
        serviceHandler = Handler(handlerThread.looper)
    }

    // Prepare notification + start foreground service
    private fun startForegroundService(): NotificationCompat.Builder {

        val pendingIntent = getPendingIntent()
        val channelId = createNotificationChannel()

        val notificationBuilder = getNotificationBuilder(pendingIntent, channelId)

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
        return notificationBuilder
    }

    // PendingIntent → click notification → open MainActivity
    private fun getPendingIntent(): PendingIntent {
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
            PendingIntent.FLAG_IMMUTABLE else 0

        return PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            flag
        )
    }

    // Create notification channel (API 26+)
    private fun createNotificationChannel(): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channelId = "001"
            val channelName = "001 Channel"
            val channelPriority = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, channelPriority)

            val service = requireNotNull(
                ContextCompat.getSystemService(this, NotificationManager::class.java)
            )

            service.createNotificationChannel(channel)
            channelId

        } else {
            ""
        }

    // Build notification content
    private fun getNotificationBuilder(
        pendingIntent: PendingIntent,
        channelId: String
    ) = NotificationCompat.Builder(this, channelId)
        .setContentTitle("Second worker process is done")
        .setContentText("Check it out!")
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentIntent(pendingIntent)
        .setTicker("Second worker process is done, check it out!")
        .setOngoing(true)

    // ============================================
    // STEP 5 & 6: onStartCommand + countdown logic
    // ============================================

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val returnValue = super.onStartCommand(intent, flags, startId)

        val Id = intent?.getStringExtra(EXTRA_ID)
            ?: throw IllegalStateException("Channel ID must be provided")

        // Run countdown in background thread
        serviceHandler.post {

            // Step 1: countdown
            countDownFromTenToZero(notificationBuilder)

            // Step 2: notify MainActivity via LiveData
            notifyCompletion(Id)

            // Step 3: stop foreground notification
            stopForeground(STOP_FOREGROUND_REMOVE)

            // Step 4: destroy service
            stopSelf()
        }

        return returnValue
    }

    // Countdown 10 → 0 and update notification text every second
    private fun countDownFromTenToZero(notificationBuilder: NotificationCompat.Builder) {

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        for (i in 10 downTo 0) {
            Thread.sleep(1000L)

            notificationBuilder
                .setContentText("$i seconds until last warning")
                .setSilent(true)

            notificationManager.notify(
                NOTIFICATION_ID,
                notificationBuilder.build()
            )
        }
    }

    // Update LiveData on main thread to notify MainActivity
    private fun notifyCompletion(Id: String) {
        Handler(Looper.getMainLooper()).post {
            mutableID.value = Id
        }
    }

    // Companion object
    companion object {
        const val NOTIFICATION_ID = 0xCA7
        const val EXTRA_ID = "Id"

        private val mutableID = MutableLiveData<String>()
        val trackingCompletion: LiveData<String> = mutableID
    }
}

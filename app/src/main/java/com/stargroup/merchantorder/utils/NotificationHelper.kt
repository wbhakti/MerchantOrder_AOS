package com.stargroup.merchantorder.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.stargroup.merchantorder.HomeActivity
import com.stargroup.merchantorder.R

class NotificationHelper(
    private val context: Context
) {

    companion object {
        const val CHANNEL_ID = "merchant_channel"
        const val CHANNEL_NAME = "Merchant Notification"
        const val CHANNEL_DESC = "Notifikasi Merchant Order"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC
            }

            val manager =
                context.getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(channel)
        }
    }

    fun showNotification(
        id: Int = System.currentTimeMillis().toInt(),
        title: String,
        message: String,
        transactionId: String? = null
    ) {

        val intent = Intent(context, HomeActivity::class.java).apply {

            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP

            putExtra("idTransaksi", transactionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val manager =context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        manager.notify(id, notification)
    }
}
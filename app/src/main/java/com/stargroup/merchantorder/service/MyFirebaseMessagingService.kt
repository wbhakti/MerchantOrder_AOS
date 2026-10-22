package com.stargroup.merchantorder.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.stargroup.merchantorder.utils.NotificationEvent
import com.stargroup.merchantorder.utils.NotificationHelper

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {

        super.onNewToken(token)

        Log.d("FCM TOKEN", token)

        // Kirim token ke Laravel
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "Pesan diterima")
        Log.d("FCM", "From: ${remoteMessage.data["title"]}")
        Log.d("FCM", "From: ${remoteMessage.data["body"]}")
        Log.d("FCM", "From: ${remoteMessage.from}")
        Log.d("FCM", "Data: ${remoteMessage.data}")

        val title = remoteMessage.data["title"] ?: "Merchant Order"

        val body = remoteMessage.data["body"] ?: ""

        val transactionId = remoteMessage.data["idTransaksi"]

        NotificationHelper(this).showNotification(
            title = title,
            message = body,
            transactionId = transactionId
        )

        // Beritahu HomeActivity
        if (transactionId != null) {
            NotificationEvent.notifyNewOrder(transactionId)
        }
    }

}
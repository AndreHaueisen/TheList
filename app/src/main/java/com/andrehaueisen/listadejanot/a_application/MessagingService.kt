package com.andrehaueisen.listadejanot.a_application

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.f_politician_selector.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.utilities.INTENT_POLITICIAN_NAME
import com.andrehaueisen.listadejanot.utilities.NEW_POLITICIAN_CHANNEL
import com.andrehaueisen.listadejanot.utilities.NOTIFICATION_CHANNEL_ID
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*


/**
 * Created by andre on 7/17/2017.
 */
class MessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        val data = remoteMessage.data
        val politicianName = data["name"]

        val contentTitle = getString(R.string.notification_title)
        val contentText = getString(R.string.notification_text, politicianName)

        val intent = Intent(this, PoliticianSelectorPresenterActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intent.putExtra(INTENT_POLITICIAN_NAME, politicianName)

        val RED = 127
        val GREEN = 0
        val BLUE = 0

        val notificationBuilder = getNotificationBuilder()
        val uniqueId : Int = ((Date().time / 1000L).toInt() % Integer.MAX_VALUE)
        val pendingIntent = PendingIntent.getActivity(this, uniqueId, intent, PendingIntent.FLAG_ONE_SHOT)

        notificationBuilder.setSmallIcon(R.drawable.ic_broom_24dp)
        notificationBuilder.setColor(Color.argb(1, RED, GREEN, BLUE))
        notificationBuilder.setContentTitle(contentTitle)
        notificationBuilder.setContentText(contentText)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(uniqueId, notificationBuilder.build())
    }

    private fun getNotificationBuilder(): Notification.Builder = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        if (!channelExists()) {
            createNotificationChannel()
        }
        Notification.Builder(this, NOTIFICATION_CHANNEL_ID)

    } else {
        Notification.Builder(this)
    }

    private fun channelExists(): Boolean {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID)

            return channel != null
        }

        return false
    }

    private fun createNotificationChannel() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = NOTIFICATION_CHANNEL_ID
            val channelName = NEW_POLITICIAN_CHANNEL
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(channelId, channelName, importance)

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }

    }
}
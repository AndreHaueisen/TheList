package com.andrehaueisen.listadejanot.a_application

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import com.andrehaueisen.listadejanot.R
import com.andrehaueisen.listadejanot.e_search_politician.mvp.PoliticianSelectorPresenterActivity
import com.andrehaueisen.listadejanot.utilities.INTENT_POLITICIAN_NAME
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by andre on 7/17/2017.
 */
class MessagingService: FirebaseMessagingService() {

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

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        val notificationBuilder = Notification.Builder(this)
        notificationBuilder.setSmallIcon(R.drawable.ic_janot_24dp)
        notificationBuilder.setColor(Color.argb(1, RED, GREEN, BLUE))
        notificationBuilder.setContentTitle(contentTitle)
        notificationBuilder.setContentText(contentText)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())
    }
}
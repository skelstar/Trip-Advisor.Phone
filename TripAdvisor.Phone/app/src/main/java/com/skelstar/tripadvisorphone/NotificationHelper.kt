package com.skelstar.android.notificationchannels

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import com.skelstar.tripadvisorphone.MainActivity
import com.skelstar.tripadvisorphone.R


fun createTripNotification(context: Context, id: Int, text: String, body: String) {

    val helper = NotificationHelper(context)

    when (id) {
        MainActivity.TRIP_NOTIFY_ID -> helper.notify(id, helper.getTripNotification(text, body))
    }
}


internal class NotificationHelper
    (ctx: Context) : ContextWrapper(ctx) {

    private val manager: NotificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    init {
        val chan1 = NotificationChannel(PRIMARY_CHANNEL,
            getString(R.string.noti_channel_default), NotificationManager.IMPORTANCE_DEFAULT)
        chan1.lightColor = Color.GREEN
        chan1.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        manager.createNotificationChannel(chan1)
    }

    fun getTripNotification(text: String, body: String): Notification.Builder {

        return Notification.Builder(applicationContext, PRIMARY_CHANNEL)
            .setContentTitle("Your trip")
            .setContentText(text)
            .setStyle(Notification.BigTextStyle().bigText(body))
            .setSmallIcon(smallIcon)
            .setAutoCancel(true)
    }

    fun notify(id: Int, notification: Notification.Builder) {
        manager.notify(id, notification.build())
    }

    private val smallIcon: Int
        get() = R.drawable.ic_notification


    companion object {
        val PRIMARY_CHANNEL = "default"
        val SECONDARY_CHANNEL = "second"
    }
}

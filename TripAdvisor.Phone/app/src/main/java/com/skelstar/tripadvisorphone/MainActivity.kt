package com.skelstar.tripadvisorphone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.skelstar.android.notificationchannels.NotificationHelper

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var helper: NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        helper = NotificationHelper(this)

        btnNotify.setOnClickListener { view ->
            sendTripNotification(TRIP_NOTIFY_ID, "Your trip")
        }
    }

    private fun sendTripNotification(id: Int, title: String) {
        when (id) {
            TRIP_NOTIFY_ID -> helper.notify(id, helper.getTripNotification())
        }
    }


    companion object {
        private val TRIP_NOTIFY_ID = 1100
    }
}

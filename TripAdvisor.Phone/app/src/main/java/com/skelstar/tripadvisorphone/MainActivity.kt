package com.skelstar.tripadvisorphone

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
            sendNotification(NOTI_PRIMARY1, "Your trip")
        }
    }

    private fun sendNotification(id: Int, title: String) {
        when (id) {
            NOTI_PRIMARY1 -> helper.notify(id, helper.getNotification1(title, getString(R.string.primary1_body)))
        }
    }


    companion object {
        private val NOTI_PRIMARY1 = 1100
    }
}

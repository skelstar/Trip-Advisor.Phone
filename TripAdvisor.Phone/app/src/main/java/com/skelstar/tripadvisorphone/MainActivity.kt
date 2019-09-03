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

        fab.setOnClickListener { view ->
            sendNotification(NOTI_PRIMARY1, "title")
        }
    }

    fun sendNotification(id: Int, title: String) {
        when (id) {
            NOTI_PRIMARY1 -> helper.notify(
                    id, helper.getNotification1(title, getString(R.string.primary1_body)))
//            NOTI_PRIMARY2 -> helper.notify(
//                    id, helper.getNotification1(title, getString(R.string.primary2_body)))
//            NOTI_SECONDARY1 -> helper.notify(
//                    id, helper.getNotification2(title, getString(R.string.secondary1_body)))
//            NOTI_SECONDARY2 -> helper.notify(
//                    id, helper.getNotification2(title, getString(R.string.secondary2_body)))
        }
    }


    companion object {
        private val TAG = MainActivity::class.java.simpleName

        private val NOTI_PRIMARY1 = 1100
//        private val NOTI_PRIMARY2 = 1101
//        private val NOTI_SECONDARY1 = 1200
//        private val NOTI_SECONDARY2 = 1201
    }
}

package com.skelstar.tripadvisorphone

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.skelstar.android.notificationchannels.NotificationHelper

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.jetbrains.anko.toast

class MainActivity : AppCompatActivity() {

    private lateinit var helper: NotificationHelper
    var m_bluetoothAdapter: BluetoothAdapter? = null
    lateinit var m_pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        helper = NotificationHelper(this)

        btnNotify.setOnClickListener { view ->
            sendTripNotification(TRIP_NOTIFY_ID, "Your trip")
        }

        select_device_refresh.setOnClickListener()

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if(m_bluetoothAdapter == null) {
            toast("Doesn't support bluetooth")
            return
        }

        // https://www.youtube.com/watch?v=Oz4CBHrxMMs&t=1752s (at 25:57)

//        if (!m_bluetoothAdapter.isEnabled)
    }

    private fun sendTripNotification(id: Int, title: String) {
        when (id) {
            TRIP_NOTIFY_ID -> helper.notify(id, helper.getTripNotification())
        }
    }

    private fun pairedDeviceList() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
    }


    companion object {
        private val TRIP_NOTIFY_ID = 1100
    }
}

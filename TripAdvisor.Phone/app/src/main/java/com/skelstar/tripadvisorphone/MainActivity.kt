package com.skelstar.tripadvisorphone

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.skelstar.android.notificationchannels.NotificationHelper
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var helper: NotificationHelper

    var m_bluetoothAdapter: BluetoothAdapter? = null
    lateinit var m_pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1
    var mBluetoothGatt: BluetoothGatt? = null

    val deviceESP32DevUUID: String = "80:7D:3A:C5:6A:36"
    val deviceOfInterestUUID2: String = "58:B1:0F:7A:FF:B1"
    val deviceM5StackUUID = "30:AE:A4:4F:A5:2A"

    var trip: TripData = TripData(volts = 0f, amphours = 0f, distance = 0f)
    var mHandler: Handler? = null // Our main handler that will receive callback notifications


    companion object {
        var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var m_bluetoothSocket: BluetoothSocket? = null
        //        lateinit var m_progress: ProgressDialog
        lateinit var m_bluetoothAdapter: BluetoothAdapter
        var m_isConnected: Boolean = false
        lateinit var m_address: String
        val TRIP_NOTIFY_ID = 1100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        ConnectToDevice(this).execute()
    }

    private fun sendCommand(input: String) {
        if (m_bluetoothSocket != null) {
            try{
                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
            } catch(e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun disconnect() {
        if (m_bluetoothSocket != null) {
            try {
                m_bluetoothSocket!!.close()
                m_bluetoothSocket = null
                m_isConnected = false
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        finish()
    }

    private class ConnectToDevice(c: Context) : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true
        private val context: Context

        init {
            this.context = c
        }

        override fun onPreExecute() {
            super.onPreExecute()
//            m_progress = ProgressDialog.show(context, "Connecting...", "please wait")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
                if (m_bluetoothSocket == null || !m_isConnected) {
                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    m_bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                m_isConnected = true
            }
//            m_progress.dismiss()
        }
    }
//
//    fun bleConnect() {
//
//        object : Thread() {
//            override fun run() {
//                var fail = false
//
//                val device = m_bluetoothAdapter?.getRemoteDevice(deviceESP32DevUUID)
//                Log.i("BLE", "Connecting bluetooth")
//
//                try {
//                    mBTSocket = createBluetoothSocket(device!!)
//                } catch (e: IOException) {
//                    fail = true
//                    Toast.makeText(baseContext, "Socket creation failed", Toast.LENGTH_SHORT).show()
//                }
//
//                // Establish the Bluetooth socket connection.
//                try {
//                    Log.i("bleConnect", "Trying to connect")
//                    mBTSocket?.connect()
//                } catch (e: IOException) {
//                    try {
//                        fail = true
//                        mBTSocket?.close()
//                        mHandler?.obtainMessage(CONNECTING_STATUS, -1, -1)
//                            ?.sendToTarget()
//                    } catch (e2: IOException) {
//                        //insert code to deal with this
//                        Toast.makeText(baseContext, "Socket creation failed", Toast.LENGTH_SHORT)
//                            .show()
//                    }
//
//                }
//
//                if (fail == false) {
//                    Log.i("bleConnect", "Didn't fail")
//                    mConnectedThread = ConnectedThread(mBTSocket!!)
//                    mConnectedThread.start()
//
//                    mHandler?.obtainMessage(CONNECTING_STATUS, 1, -1, name)?.sendToTarget()
//                }
//            }
//        }.start()
//    }   // bleConnect()


    fun toastAsync(message: String) {
        val message = mHandler?.obtainMessage(1, message)
        message?.sendToTarget()
    }

}   // end class



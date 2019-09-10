package com.skelstar.tripadvisorphone

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.skelstar.android.notificationchannels.NotificationHelper
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.IOException
import java.util.*
import java.nio.file.Files.delete
import android.widget.Toast
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.schedule


//import sun.text.normalizer.UTF16.append




//companion object {
    var m_myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    var m_bluetoothSocket: BluetoothSocket? = null
    //        lateinit var m_progress: ProgressDialog
    lateinit var m_bluetoothAdapter: BluetoothAdapter
    var m_isConnected: Boolean = false
    var m_address: String = "80:7D:3A:C5:6A:36"
    val TRIP_NOTIFY_ID = 1100
var mHandler: Handler? = null // Our main handler that will receive callback notifications
var bluetoothInHandler: Handler? = null
var handlerState: Int = 0
private val recDataString = StringBuilder()

//}

class MainActivity : AppCompatActivity() {

    private lateinit var helper: NotificationHelper
    lateinit var mConnectedThread: ConnectedThread

//    val deviceESP32DevUUID: String = "80:7D:3A:C5:6A:36"
//    val deviceOfInterestUUID2: String = "58:B1:0F:7A:FF:B1"
//    val deviceM5StackUUID = "30:AE:A4:4F:A5:2A"

    var trip: TripData = TripData(volts = 0f, amphours = 0f, distance = 0f)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnConnect.setOnClickListener { _ ->
            //ConnectToDevice(this).execute()
            sendCommand("test")
        }

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                Log.i("Handler", msg.obj as String)
                toast(msg.obj as String)
            }
        }

        bluetoothInHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == handlerState) {                                     //if message is what we want
                    val readMessage = msg.obj as String                                                                // msg.arg1 = bytes from connect thread
                    recDataString.append(readMessage)                                      //keep appending to string until ~
                    val endOfLineIndex =
                        recDataString.indexOf("~")                    // determine the end-of-line
                    if (endOfLineIndex > 0) {                                           // make sure there data before ~
                        var dataInPrint =
                            recDataString.substring(0, endOfLineIndex)    // extract string
                        Log.i("handleMessage", dataInPrint)
                        //txtString.setText("Data Received = $dataInPrint")
                        val dataLength = dataInPrint.length                          //get length of data received
                        //txtStringLength.setText("String Length = " + dataLength.toString())

//                        if (recDataString.charAt(0) === '#')
//                        //if it starts with # we know it is what we are looking for
//                        {
//                            val sensor0 = recDataString.substring(
//                                1,
//                                5
//                            )             //get sensor value from string between indices 1-5
//                            val sensor1 = recDataString.substring(6, 10)            //same again...
//                            val sensor2 = recDataString.substring(11, 15)
//                            val sensor3 = recDataString.substring(16, 20)
//
//                            sensorView0.setText(" Sensor 0 Voltage = " + sensor0 + "V")    //update the textviews with sensor values
//                            sensorView1.setText(" Sensor 1 Voltage = " + sensor1 + "V")
//                            sensorView2.setText(" Sensor 2 Voltage = " + sensor2 + "V")
//                            sensorView3.setText(" Sensor 3 Voltage = " + sensor3 + "V")
//                        }
//                        recDataString.delete(
//                            0,
//                            recDataString.length()
//                        )                    //clear all string data
                        // strIncom =" ";
                        dataInPrint = " "
                    }
                }
            }
        }

        try {
            if (m_bluetoothSocket == null || !m_isConnected) {
                Log.i("onCreate", "Connecting...")
                m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
                val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
                if (device != null) {
                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
                    if (m_bluetoothSocket == null) {
                        Log.i("", "Error creating bluetooth socket")
                    }
                    else {
                        Log.i("", "OK")
                    }
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()

                    Timer().schedule(1000){
                        m_bluetoothSocket!!.connect()
                        Log.i("onCreate", "Connected")
                        mConnectedThread = ConnectedThread(m_bluetoothSocket!!);
                        Log.i("onCreate", "Starting thread")
                        mConnectedThread.start();
                    }
                }
                else {
                    Log.i("onCreate", "Couldn't find device")
                }
            }
        } catch (e: IOException) {
//            connectSuccess = false
            Log.i("onCreate", "Error connecting: " + e.message)
//            e.printStackTrace()
        }
//        ConnectToDevice(this).execute()
    }

    private fun sendCommand(input: String) {
        if (mConnectedThread != null) {
            mConnectedThread.write(input)
        } else {
            Log.i("sendCommand", "mConnectedThread not initialised")
        }
//        if (m_bluetoothSocket != null) {
//            try{
//                m_bluetoothSocket!!.outputStream.write(input.toByteArray())
//            } catch(e: IOException) {
//                e.printStackTrace()
//            }
//        }
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
            Log.i("onPreExecute", "Connecting...")
        }

        override fun doInBackground(vararg p0: Void?): String? {
            try {
//                if (m_bluetoothSocket == null || !m_isConnected) {
//                    m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//                    val device: BluetoothDevice = m_bluetoothAdapter.getRemoteDevice(m_address)
//                    m_bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(m_myUUID)
//                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
//                    m_bluetoothSocket!!.connect()
//                    mConnectedThread = ConnectedThread(m_bluetoothSocket!!);
//                    mConnectedThread.start();
//                }
            } catch (e: IOException) {
                connectSuccess = false
//                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
                this.context.toast("Couldn't connect :(")
            } else {
                m_isConnected = true
                Log.i("onPostExecute", "Connected!")
                this.context.toast("Connected!")
            }
        }

        //create new class for connect thread
    }

    inner class ConnectedThread//creation of the connect thread
        (socket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            try {
                //Create I/O streams for connection
                tmpIn = socket.inputStream
                tmpOut = socket.outputStream
            } catch (e: IOException) {
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(256)
            var bytes: Int

            // Keep looping to listen for received messages
            Log.i("ConnectedThread", "running")
            while (true) {
                try {
                    bytes = mmInStream!!.read(buffer)            //read bytes from input buffer
                    val readMessage = String(buffer, 0, bytes)
                    //bluetoothInHandler?.obtainMessage(handlerState, bytes, -1, readMessage)?.sendToTarget()
                } catch (e: IOException) {
                    break
                }
            }
        }

        //write method
        fun write(input: String) {
            val msgBuffer = input.toByteArray()           //converts entered String into bytes
            try {
                Log.i("COnnected Thread", "Writing")
                mmOutStream!!.write(msgBuffer)                //write bytes over BT connection via outstream
            } catch (e: IOException) {
                //if you cannot write, close the application
                //Toast.makeText(baseContext, "Connection Failure", Toast.LENGTH_LONG).show()
                finish()
            }

        }
    }
}   // end class



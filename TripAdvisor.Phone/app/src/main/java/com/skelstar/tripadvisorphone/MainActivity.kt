package com.skelstar.tripadvisorphone

import android.app.Activity
import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.skelstar.android.notificationchannels.NotificationHelper
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
//import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import android.os.Looper
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGatt
import android.content.Context
import android.os.Message
import android.widget.TextView
import android.widget.Toast
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.skelstar.android.notificationchannels.sendTripNotification
import org.jetbrains.anko.ctx


class MainActivity : AppCompatActivity() {

    private lateinit var helper: NotificationHelper

    var m_bluetoothAdapter: BluetoothAdapter? = null
    lateinit var m_pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1
    var mBluetoothGatt:BluetoothGatt ?= null
    var bleCharacteristic: BluetoothGattCharacteristic ?= null

    val deviceESP32DevUUID:String = "80:7D:3A:C5:6A:36"
    val deviceOfInterestUUID2:String = "58:B1:0F:7A:FF:B1"
    val deviceM5StackUUID = "30:AE:A4:4F:A5:2A"

    var trip: TripData = TripData(volts = 0f, amphours = 0f)

    lateinit var mHandler: Handler


    companion object {
        val TRIP_NOTIFY_ID = 1100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        btnConnect.setOnClickListener { _ ->
            bleConnect()
        }

        select_device_refresh.setOnClickListener{ }

        mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                Log.i("Handler", msg.obj as String)
                toast(msg.obj as String)
            }
        }

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null) {
            Toast.makeText(this, "this device doesn't support bluetooth", Toast.LENGTH_SHORT).show()
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        bleConnect()
    }

    fun bleConnect() {

        val deviceOfInterest = m_bluetoothAdapter?.getRemoteDevice(deviceESP32DevUUID)    // findTheDeviceOfInterest()
        if (deviceOfInterest != null) {
            mBluetoothGatt = deviceOfInterest.connectGatt(this, false, mBleGattCallBack)
            if (mBluetoothGatt != null) {
                Log.i("ble", "mBluetoothGatt != null")
            }
        }
    }

    fun toastAsync(message: String) {
        val message = mHandler.obtainMessage(1, message)
        message.sendToTarget()
    }

    private val mBleGattCallBack: BluetoothGattCallback by lazy {
        object : BluetoothGattCallback(){

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.i("ble", "onConnectionStateChange: ${DeviceProfile.getStateDescription(newState)} = ${DeviceProfile.getStatusDescription(status)}")
                if(newState == BluetoothProfile.STATE_CONNECTED){
                    Timer().schedule(1000){
                        gatt!!.requestMtu(128)  // bigger packet size
                        mBluetoothGatt?.discoverServices()
                        toastAsync("Connected")
                    }
                }
                else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    toastAsync("Disconnected!")
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                //BLE service discovery complete
                super.onServicesDiscovered(gatt, status)

                Log.i("BLE", "Services discovered!")

                val characteristic = getCharacteristic(gatt!!)

                Timer().schedule(200) {
                    gatt?.setCharacteristicNotification(characteristic!!, true)
                    Log.i("BLE", "setCharacteristicNotification")
                }

                if (characteristic != null) {
                    Timer().schedule(200) {
                        enableNotification(gatt, characteristic)
                        Log.i("BLE", "enableNotification")
                    }
                }
            }

            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                super.onCharacteristicRead(gatt, characteristic, status)
                Log.i("ble","onCharacteristicRead: value ${characteristic?.getStringValue(0)}")
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)
//                Log.i("ble","onCharacteristicChanged: value ${characteristic?.getStringValue(0)}")

                val data = String(characteristic?.value!!)
                val mapper = jacksonObjectMapper()
                trip = mapper.readValue(data)

//                sendTripNotification(ctx, TRIP_NOTIFY_ID, "batt: ${trip.volts}")
                Log.i("ble","onCharacteristicChanged: volts = ${trip.volts}v amphours = ${trip.amphours}AH")
            }
        }
    }

    fun toast(message:CharSequence) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BLUETOOTH) {
            if (resultCode == Activity.RESULT_OK) {
                if (m_bluetoothAdapter!!.isEnabled) {
                    toast("Bluetooth has been enabled")
                } else {
                    toast("Bluetooth has been disabled")
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                toast("Bluetooth enabling has been canceled")
            }
        }
    }
}



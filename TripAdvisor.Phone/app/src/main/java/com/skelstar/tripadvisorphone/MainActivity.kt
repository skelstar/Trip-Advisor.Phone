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
import org.jetbrains.anko.toast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import android.os.Looper
import androidx.fragment.app.FragmentActivity


class MainActivity : AppCompatActivity() {

    private lateinit var helper: NotificationHelper

    var m_bluetoothAdapter: BluetoothAdapter? = null
    lateinit var m_pairedDevices: Set<BluetoothDevice>
    val REQUEST_ENABLE_BLUETOOTH = 1
    var mBluetoothGatt:BluetoothGatt ?= null
    var bleCharacteristic: BluetoothGattCharacteristic ?= null
    val deviceOfInterestUUID:String = "80:7D:3A:C5:6B:0E"
    val deviceOfInterestUUID2:String = "58:B1:0F:7A:FF:B1"


    companion object {
        private val TRIP_NOTIFY_ID = 1100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        helper = NotificationHelper(this)

        btnNotify.setOnClickListener { _ ->
            sendTripNotification(TRIP_NOTIFY_ID, "Your trip")
        }

        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if(m_bluetoothAdapter == null) {
            toast("this device doesn't support bluetooth")
            return
        }
        if(!m_bluetoothAdapter!!.isEnabled) {
            val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBluetoothIntent, REQUEST_ENABLE_BLUETOOTH)
        }

        select_device_refresh.setOnClickListener{ pairedDeviceList() }

        val deviceOfInterest = m_bluetoothAdapter?.getRemoteDevice(deviceOfInterestUUID)    // findTheDeviceOfInterest()
        if (deviceOfInterest != null) {
            mBluetoothGatt = deviceOfInterest.connectGatt(this, false, mBleGattCallBack)
            if (mBluetoothGatt != null) {
                Log.i("ble", "mBluetoothGatt != null")
            }

        }
    }

    private val mBleGattCallBack: BluetoothGattCallback by lazy {
        object : BluetoothGattCallback(){

            override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                super.onConnectionStateChange(gatt, status, newState)
                Log.i("ble", "onConnectionStateChange: ${DeviceProfile.getStateDescription(newState)}, ${DeviceProfile.getStatusDescription(status)}")
                if(newState == BluetoothProfile.STATE_CONNECTED){
//                    Log.i("ble", "Discovering services")
                    Timer().schedule(1000){
                        Log.i("ble", "Timer")
                        Handler(Looper.getMainLooper()).post(Runnable {
                            val ans = mBluetoothGatt?.discoverServices()
                            Log.i("ble", "Discover Services started: $ans")
                        })

//                        mBluetoothGatt?.discoverServices()
//                        val service = mBluetoothGatt?.getService(DeviceProfile.SERVICE_UUID)
//                        if (service != null) {
//                            val characteristic = service?.getCharacteristic(DeviceProfile.CHARACTERISTIC_STATE_UUID)
//                            if (characteristic == null) {
//                                Log.i("ble", "char is null")
//                            }
//                            else {
//                                mBluetoothGatt?.readCharacteristic(characteristic)
//                                //val value :Float?= characteristic?.value!![0].toFloat()
//                                Log.i("ble", "Value: " + characteristic?.value!![0])
//                            }
//                        }
//                        else {
//                            Log.i("ble", "service is null")
//                        }
                    }
                }
            }

            override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                //BLE service discovery complete
                super.onServicesDiscovered(gatt, status)
                Log.i("ble","on Services discovered")

                val characteristic = gatt?.getService(DeviceProfile.SERVICE_UUID)
                    ?.getCharacteristic(DeviceProfile.CHARACTERISTIC_STATE_UUID)

                gatt?.readCharacteristic(characteristic)

            }

            override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                super.onCharacteristicRead(gatt, characteristic, status)
                Log.i("ble","onCharacteristicRead: reading into the characteristic ${characteristic?.uuid} the value ${characteristic?.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32,0)}")
                val value :Float= characteristic?.value!![0].toFloat()
                if(DeviceProfile.CHARACTERISTIC_STATE_UUID == characteristic?.uuid){
                    gatt?.setCharacteristicNotification(characteristic,true)
                }
            }

            override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                super.onCharacteristicChanged(gatt, characteristic)
                Log.i("ble","onCharacteristicChanged: reading into the characteristic ${characteristic?.uuid} the value ${characteristic?.getIntValue(
                    BluetoothGattCharacteristic.FORMAT_UINT32,0)}")
            }
        }
    }

    private fun sendTripNotification(id: Int, title: String) {

        when (id) {
            TRIP_NOTIFY_ID -> helper.notify(id, helper.getTripNotification())
        }
    }

//    private fun findTheDeviceOfInterest(): BluetoothDevice? {
//
//        val connectedDevices = m_bluetoothAdapter!!.bondedDevices
//        for (device in connectedDevices) {
//            if (device.address == deviceOfInterestUUID) { // "24:0A:C4:0A:3C:62") {
//                Log.i("device", "found the device of interest!")
//                //m_bluetoothAdapter.bluetoothLeScanner.stopScan()
//                return device
//            }
//            else {
//                Log.i("device", "not "+device.address)
//            }
//        }
//
//        toast("Can't find device of interest!")
//        return null
//    }

    private fun pairedDeviceList() {
        Log.i("pairedDevices", "pairedDeviceLIst")
        m_pairedDevices = m_bluetoothAdapter!!.bondedDevices
        val list : ArrayList<BluetoothDevice> = ArrayList()

        if (!m_pairedDevices.isEmpty()) {
            for (device: BluetoothDevice in m_pairedDevices) {
                list.add(device)
                Log.i("device", ""+device)
            }
        } else {
            toast("no paired bluetooth devices found")
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        select_device_list.adapter = adapter
        select_device_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val device: BluetoothDevice = list[position]
        }
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



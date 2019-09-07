package com.skelstar.tripadvisorphone

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothProfile
import java.util.*

class DeviceProfile{

    companion object {
        var SERVICE_UUID = UUID.fromString("4fafc201-1fb5-459e-8fcc-c5c9c331914b")

        //Read-Wrote only characteristic providing the state of the lamp
        var CHARACTERISTIC_STATE_UUID = UUID.fromString("beb5483e-36e1-4688-b7f5-ea07361b26a8")
        val CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")

        fun getStateDescription(state: Int): String {
            return when (state) {
                BluetoothProfile.STATE_CONNECTED -> "Connected"
                BluetoothProfile.STATE_CONNECTING ->  "Connecting"
                BluetoothProfile.STATE_DISCONNECTED -> "Disconnected"
                BluetoothProfile.STATE_DISCONNECTING -> "Disconnecting"
                else -> "Unknown State $state"
            }
        }

        fun getStatusDescription(status: Int): String {
            return when (status) {
                BluetoothGatt.GATT_SUCCESS ->  "SUCCESS"
                else ->  "Unknown Status $status"
            }
        }

    }
}
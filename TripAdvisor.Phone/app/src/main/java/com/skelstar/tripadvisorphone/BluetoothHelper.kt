package com.skelstar.tripadvisorphone

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import com.skelstar.tripadvisorphone.DeviceProfile.Companion.CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID


fun getCharacteristic(gatt: BluetoothGatt): BluetoothGattCharacteristic? {

    return gatt
        .getService(DeviceProfile.SERVICE_UUID)
        ?.getCharacteristic(DeviceProfile.CHARACTERISTIC_STATE_UUID)
}

fun enableNotification(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {

    gatt.setCharacteristicNotification(characteristic, true)

    val descriptor = characteristic?.getDescriptor(CHARACTERISTIC_UPDATE_NOTIFICATION_DESCRIPTOR_UUID)
    descriptor?.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
    gatt?.writeDescriptor(descriptor)
}
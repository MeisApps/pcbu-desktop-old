package com.meisapps.pcbiounlock.service.api

import com.meisapps.pcbiounlock.utils.extensions.toKBytes
import com.meisapps.pcbiounlock.utils.extensions.toKString
import com.sun.jna.ptr.IntByReference

object BluetoothApi {
    private var isBTAvailable = PCBUApi.INSTANCE.bt_is_available()

    data class BluetoothDevice(val name: String, val address: String) {
        override fun toString(): String {
            return name
        }
    }

    fun isBluetoothAvailable(): Boolean {
        return isBTAvailable
    }

    @Suppress("UNCHECKED_CAST")
    fun scanForDevices(): List<BluetoothDevice> {
        val pCount = IntByReference()
        val devicesPtr = PCBUApi.INSTANCE.bt_scan_devices(pCount)

        if(devicesPtr != null) {
            val devices = if(pCount.value > 0)
                devicesPtr.toArray(pCount.value) as Array<PCBUBluetoothDevice>
            else
                emptyArray()

            val list = ArrayList<BluetoothDevice>()
            for(device in devices) {
                list.add(BluetoothDevice(device.name.toKString(Charsets.UTF_8),
                    device.address.toKString(Charsets.UTF_8)))
            }

            PCBUApi.INSTANCE.api_free(devicesPtr.pointer)
            return list
        }

        return emptyList()
    }

    fun pairDevice(device: BluetoothDevice): Boolean {
        val btDevice = PCBUBluetoothDevice()
        btDevice.name = device.name.toKBytes(Charsets.UTF_8, 255)
        btDevice.address = device.address.toKBytes(Charsets.UTF_8, 18)
        return PCBUApi.INSTANCE.bt_pair_device(btDevice)
    }
}

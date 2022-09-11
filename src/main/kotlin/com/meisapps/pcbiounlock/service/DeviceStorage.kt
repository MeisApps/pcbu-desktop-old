package com.meisapps.pcbiounlock.service

import com.meisapps.pcbiounlock.service.linux.PamDeviceStorage
import com.meisapps.pcbiounlock.service.windows.WinDeviceStorage
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import kotlinx.serialization.Serializable

@Serializable
data class PairedDevice(val pairingId: String, val deviceName: String,
                           val messagingToken: String,
                           val ipAddress: String, val bluetoothAddress: String,
                           val encryptionKey: String,
                           val userName: String)

abstract class DeviceStorage {
    companion object {
        private var storageObj: DeviceStorage? = null

        fun getForPlatform(shell: Shell): DeviceStorage? {
            if(storageObj != null)
                return storageObj

            storageObj = if(OperatingSystem.isLinux)
                            PamDeviceStorage(shell)
                         else if(OperatingSystem.isWindows)
                            WinDeviceStorage()
                         else null
            return storageObj
        }
    }

    fun init() {
        if(!isConfigValid()) {
            clearData()
        }
    }

    abstract fun clearData()
    abstract fun isConfigValid() : Boolean
    abstract fun isPaired() : Boolean

    abstract fun getDeviceName() : String
    abstract fun savePairData(pairingId: String, deviceName: String, messagingToken: String, ipAddress: String, bluetoothAddress: String, encryptionKey: String)
}
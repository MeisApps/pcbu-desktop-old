package com.meisapps.pcbiounlock.service.api

import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.exceptions.ErrorMessageException
import com.meisapps.pcbiounlock.utils.extensions.toKString
import com.meisapps.pcbiounlock.utils.host.HostOS
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import com.meisapps.pcbiounlock.utils.text.I18n
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.ptr.IntByReference
import java.io.File

@Structure.FieldOrder("name", "address")
class PCBUBluetoothDevice : Structure() {
    @JvmField var name = ByteArray(255)
    @JvmField var address = ByteArray(18)
}

@Structure.FieldOrder("ipAddr", "macAddr")
class PCBUIpAndMac : Structure() {
    @JvmField var ipAddr = ByteArray(16)
    @JvmField var macAddr = ByteArray(18)
}

interface PCBULibrary : Library {
    fun api_free(ptr: Pointer)
    fun get_local_ip_and_mac(): PCBUIpAndMac?

    fun bt_is_available(): Boolean
    fun bt_scan_devices(count: IntByReference): PCBUBluetoothDevice?
    fun bt_get_paired_devices(count: IntByReference): PCBUBluetoothDevice?
    fun bt_pair_device(device: PCBUBluetoothDevice): Boolean
}

object PCBUApi {
    val INSTANCE: PCBULibrary

    init {
        System.setProperty("jna.library.path", System.getProperty("java.io.tmpdir"));
        val serviceInstaller = ServiceInstaller.getForPlatform(Shell.getForPlatform()!!)!!
        if(!serviceInstaller.isOpenSSLInstalled())
            serviceInstaller.installOpenSSL()

        val libFile = ResourceHelper.getNativeModule()
        val tmpFile = File.createTempFile("libpcbiounlock", when(OperatingSystem.get()) {
            HostOS.LINUX -> ".so"
            HostOS.WINDOWS -> ".dll"
            else -> throw ErrorMessageException(I18n.get("unsupported_os"))
        })

        tmpFile.deleteOnExit()
        tmpFile.writeBytes(libFile)

        INSTANCE = Native.load(tmpFile.absolutePath, PCBULibrary::class.java) as PCBULibrary
    }

    fun getLocalIP(): String {
        val ptr = INSTANCE.get_local_ip_and_mac()
        if(ptr == null) {
            Console.println("Error getting local ip address!")
            return I18n.get("error")
        }

        val str = ptr.ipAddr.toKString(Charsets.UTF_8)
        INSTANCE.api_free(ptr.pointer)
        return str
    }

    fun getMacAddress(): String {
        val ptr = INSTANCE.get_local_ip_and_mac()
        if(ptr == null) {
            Console.println("Error getting mac address!")
            return ""
        }

        val str = ptr.macAddr.toKString(Charsets.UTF_8)
        INSTANCE.api_free(ptr.pointer)
        return str
    }
}

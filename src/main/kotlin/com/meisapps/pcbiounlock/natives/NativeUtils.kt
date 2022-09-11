package com.meisapps.pcbiounlock.natives

import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.AESUtils
import com.meisapps.pcbiounlock.utils.ErrorMessageException
import com.meisapps.pcbiounlock.utils.host.HostOS
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.text.I18n


abstract class NativeUtils {
    companion object {
        fun getForPlatform(): NativeUtils {
            return when(OperatingSystem.get()) {
                HostOS.LINUX -> LinuxUtils.instance
                HostOS.WINDOWS -> WinUtils.instance
                else -> throw ErrorMessageException(I18n.get("unsupported_os"))
            }
        }

        fun getDeviceUUID(): String {
            val shell = Shell.getForPlatform()!!
            val uuid = when(OperatingSystem.get()) {
                HostOS.LINUX -> shell.runUserCommand("cat /etc/machine-id").output.trim()
                HostOS.WINDOWS -> shell.runCommand("wmic csproduct get uuid").output.replace("UUID", "").trim()
                else -> throw Exception("Could not get device uuid !")
            }
            if(uuid.isBlank())
                throw Exception("Could not get device uuid !")
            return AESUtils.sha1(uuid)
        }
    }

    abstract fun getAllUsers(): List<String>
    abstract fun getCurrentUserName(): String
    abstract fun checkUserLogin(userName: String, password: String): Boolean
}
package com.meisapps.pcbiounlock.natives

import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.ErrorMessageException
import com.meisapps.pcbiounlock.utils.host.HostOS
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.text.I18n
import com.sun.jna.platform.win32.Advapi32Util
import com.sun.jna.platform.win32.WinReg


abstract class NativeUtils {
    companion object {
        fun getForPlatform(): NativeUtils {
            return when(OperatingSystem.get()) {
                HostOS.LINUX -> LinuxUtils.instance
                HostOS.WINDOWS -> WinUtils.instance
                else -> throw ErrorMessageException(I18n.get("unsupported_os"))
            }
        }
    }

    abstract fun getDeviceUUID(): String
    abstract fun getAllUsers(): List<String>
    abstract fun getCurrentUserName(): String
    abstract fun checkUserLogin(userName: String, password: String): Boolean
}

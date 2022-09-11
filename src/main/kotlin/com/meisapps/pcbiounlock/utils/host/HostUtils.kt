package com.meisapps.pcbiounlock.utils.host

import com.meisapps.pcbiounlock.utils.text.StringUtils
import com.meisapps.pcbiounlock.utils.io.Console
import java.net.InetAddress
import java.net.UnknownHostException


enum class HostArchitecture {
    UNKNOWN,
    X86,
    X86_64,
    ARM,
    ARM64
}

object HostUtils {
    fun getDeviceName(): String {
        var hostName = "Unknown-" + StringUtils.generateRandomHexToken(16)
        try {
            val addr = InetAddress.getLocalHost()
            hostName = addr.hostName
        } catch (ex: UnknownHostException) {
            Console.println("Hostname could not be resolved.")
        }
        return hostName
    }

    fun getCpuArchitecture(): HostArchitecture {
        return when(System.getProperty("os.arch")) {
            "amd64" -> HostArchitecture.X86_64
            "arm" -> HostArchitecture.ARM
            "aarch64" -> HostArchitecture.ARM64
            else -> HostArchitecture.UNKNOWN
        }
    }
}
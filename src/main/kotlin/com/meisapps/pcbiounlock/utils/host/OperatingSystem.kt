package com.meisapps.pcbiounlock.utils.host

enum class HostOS {
    UNKNOWN,
    WINDOWS,
    LINUX,
    MAC
}

object OperatingSystem {
    private val OS = System.getProperty("os.name", "unknown").lowercase()
    val isWindows: Boolean
        get() = OS.contains("win")
    val isMac: Boolean
        get() = OS.contains("mac")
    val isLinux: Boolean
        get() = OS.contains("nux")

    fun get(): HostOS {
        if(isWindows)
            return HostOS.WINDOWS
        if(isLinux)
            return HostOS.LINUX
        if(isMac)
            return HostOS.MAC
        return HostOS.UNKNOWN
    }

    fun getString(): String {
        return when(get()) {
            HostOS.WINDOWS -> "Windows"
            HostOS.LINUX -> "Linux"
            HostOS.MAC -> "macOS"
            HostOS.UNKNOWN -> "Unknown"
        }
    }
}

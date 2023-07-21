package com.meisapps.pcbiounlock.utils


object VersionInfo {
    fun getAppVersion(): String {
        return "1.0.4"
    }

    fun getProtocolVersion(): String {
        return "0.7.0"
    }

    fun compareVersion(thisVersion: String, otherVersion: String): Int {
        if(otherVersion.isBlank())
            return 0

        val currVer = thisVersion.replace(".", "").toInt()
        val otherVer = otherVersion.replace(".", "").toInt()
        if(currVer == otherVer)
            return 0
        if(currVer > otherVer)
            return -1
        return 1
    }
}
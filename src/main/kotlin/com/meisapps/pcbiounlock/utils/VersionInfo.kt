package com.meisapps.pcbiounlock.utils

import com.meisapps.pcbiounlock.utils.io.Console


object VersionInfo {
    fun getAppVersion(): String {
        return "1.0.5"
    }

    fun getProtocolVersion(): String {
        return "0.7.0"
    }

    fun compareVersion(thisVersion: String, otherVersion: String): Int {
        try {
            if(thisVersion.isBlank() || otherVersion.isBlank())
                return 0

            val currVer = thisVersion.replace(".", "").toInt()
            val otherVer = otherVersion.replace(".", "").toInt()

            if(currVer == otherVer)
                return 0
            if(currVer > otherVer)
                return -1
            return 1
        } catch (_: Exception) {
            Console.println("Could not check version $thisVersion and $otherVersion")
            return 0
        }
    }
}
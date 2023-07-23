package com.meisapps.pcbiounlock.natives

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.LMAccess.USER_INFO_1
import com.sun.jna.platform.win32.WinNT.HANDLE
import com.sun.jna.platform.win32.WinNT.PSID
import com.sun.jna.ptr.IntByReference
import com.sun.jna.ptr.PointerByReference
import com.sun.jna.win32.W32APITypeMapper


@FieldOrder("usri24_internet_identity", "usri24_flags", "usri24_internet_provider_name", "usri24_internet_principal_name", "usri24_user_sid")
internal class USER_INFO_24 : Structure {
    @JvmField var usri24_internet_identity = false
    @JvmField var usri24_flags = 0
    @JvmField var usri24_internet_provider_name = ""
    @JvmField var usri24_internet_principal_name = ""
    @JvmField var usri24_user_sid: PSID.ByReference? = null

    constructor() : super(W32APITypeMapper.UNICODE) {}
    constructor(memory: Pointer?) : super(memory, ALIGN_DEFAULT, W32APITypeMapper.UNICODE) {
        read()
    }
}

internal interface WTS_INFO_CLASS {
    companion object {
        const val WTSUserName = 5
        const val WTSDomainName = 7
    }
}

private val WTS_CURRENT_SERVER_HANDLE = HANDLE(null)

internal interface Kernel32Lib : Library {
    fun WTSGetActiveConsoleSessionId(): Int

    companion object {
        val INSTANCE: Kernel32Lib = Native.load("Kernel32", Kernel32Lib::class.java)
    }
}

internal interface Wtsapi32Lib : Library {
    fun WTSQuerySessionInformationA(
        hServer: HANDLE?, SessionId: Int, WTSInfoClass: Int, ppBuffer: PointerByReference?,
        pBytesReturned: IntByReference?
    ): Boolean
    fun WTSFreeMemory(pMemory: Pointer)

    companion object {
        val INSTANCE: Wtsapi32Lib = Native.load("Wtsapi32", Wtsapi32Lib::class.java)
    }
}

object WinUtils : NativeUtils() {
    val instance = this
    private const val UF_ACCOUNTDISABLE = 0x00000002
    private const val UF_LOCKOUT = 0x00000010
    private const val UF_NORMAL_ACCOUNT = 0x00000200
    private const val UF_DONT_EXPIRE_PASSWD = 0x00010000

    fun hasUserPassword(userName: String): Boolean {
        val split = userName.split("\\")
        if(split.size != 2)
            return false

        val phUser = WinNT.HANDLEByReference()
        Advapi32.INSTANCE.LogonUser(split[1], split[0], "", WinBase.LOGON32_LOGON_NETWORK, WinBase.LOGON32_PROVIDER_DEFAULT, phUser)
        val error = Kernel32.INSTANCE.GetLastError()
        if(phUser.value != null)
            Kernel32.INSTANCE.CloseHandle(phUser.value)

        return error != 1327
    }

    override fun checkUserLogin(userName: String, password: String): Boolean {
        val split = userName.split("\\")
        if(split.size != 2)
            return false

        val phUser = WinNT.HANDLEByReference()
        val result = Advapi32.INSTANCE.LogonUser(split[1], split[0], password, WinBase.LOGON32_LOGON_NETWORK, WinBase.LOGON32_PROVIDER_DEFAULT, phUser)
        if(phUser.value != null)
            Kernel32.INSTANCE.CloseHandle(phUser.value)
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun getAllUsers(): List<String> {
        val userList = ArrayList<String>()

        val computerDomain = Kernel32Util.getComputerName()
        val bufPtr = PointerByReference()
        val entriesRead = IntByReference()
        val totalEntries = IntByReference()

        val result = Netapi32.INSTANCE.NetUserEnum(null, 1, 0, bufPtr, LMCons.MAX_PREFERRED_LENGTH,
            entriesRead, totalEntries, null)
        if(result != LMErr.NERR_Success)
            throw Exception("Could not get computer users !")

        val userInfo = USER_INFO_1(bufPtr.value)
        val userInfos = userInfo.toArray(entriesRead.value) as Array<USER_INFO_1>
        for (ui in userInfos) {
            if(ui.usri1_flags and UF_ACCOUNTDISABLE == UF_ACCOUNTDISABLE ||
                ui.usri1_flags and UF_LOCKOUT == UF_LOCKOUT ||
                ui.usri1_flags and UF_NORMAL_ACCOUNT != UF_NORMAL_ACCOUNT ||
                ui.usri1_flags and UF_DONT_EXPIRE_PASSWD != UF_DONT_EXPIRE_PASSWD)
                continue

            val qualifiedName = resolveMSAccount(computerDomain, ui.usri1_name)
            userList.add(qualifiedName)
        }

        Netapi32.INSTANCE.NetApiBufferFree(bufPtr.value)
        return userList
    }

    override fun getCurrentUserName(): String {
        val id = Kernel32Lib.INSTANCE.WTSGetActiveConsoleSessionId()
        if(id.toLong() == 0xFFFFFFFF)
            throw Exception("Could not get user session !")

        val userNameBuf = PointerByReference()
        val userNameSize = IntByReference()
        var result = Wtsapi32Lib.INSTANCE.WTSQuerySessionInformationA(
            WTS_CURRENT_SERVER_HANDLE, id,
            WTS_INFO_CLASS.WTSUserName, userNameBuf, userNameSize)
        if(!result)
            throw Exception("Could not get user name !")

        val domainBuf = PointerByReference()
        val domainSize = IntByReference()
        result = Wtsapi32Lib.INSTANCE.WTSQuerySessionInformationA(
            WTS_CURRENT_SERVER_HANDLE, id,
            WTS_INFO_CLASS.WTSDomainName, domainBuf, domainSize)
        if(!result)
            throw Exception("Could not get user domain !")

        val userNameStr = userNameBuf.value.getString(0)
        val domainStr = domainBuf.value.getString(0)

        Wtsapi32Lib.INSTANCE.WTSFreeMemory(userNameBuf.value)
        Wtsapi32Lib.INSTANCE.WTSFreeMemory(domainBuf.value)

        return resolveMSAccount(domainStr, userNameStr)
    }

    private fun resolveMSAccount(domainStr: String, userNameStr: String): String {
        var qualifiedName = "$domainStr\\$userNameStr"
        val bufPtr = PointerByReference()
        val resultCode = Netapi32.INSTANCE.NetUserGetInfo(domainStr, userNameStr, 24, bufPtr)
        if (resultCode == LMErr.NERR_Success) {
            val info = USER_INFO_24(bufPtr.value)
            if(info.usri24_internet_provider_name.isNotBlank() && info.usri24_internet_principal_name.isNotBlank()) {
                qualifiedName = "${info.usri24_internet_provider_name}\\${info.usri24_internet_principal_name}"
            }
        }

        if(bufPtr.value != Pointer.NULL)
            Netapi32.INSTANCE.NetApiBufferFree(bufPtr.value)
        return qualifiedName
    }
}
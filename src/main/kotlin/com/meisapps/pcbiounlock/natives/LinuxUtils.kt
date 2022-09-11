package com.meisapps.pcbiounlock.natives

import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.utils.host.HostArchitecture
import com.meisapps.pcbiounlock.utils.host.HostUtils
import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Structure
import com.sun.jna.platform.linux.LibC
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class Password : Structure() {
    @JvmField var pw_name: String? = null
    @JvmField var pw_passwd: String? = null
    @JvmField var pw_uid = 0
    @JvmField var pw_gid = 0
    @JvmField var pw_gecos: String? = null
    @JvmField var pw_dir: String? = null
    @JvmField var pw_shell: String? = null
    override fun getFieldOrder(): List<String> {
        return listOf("pw_name", "pw_passwd", "pw_uid", "pw_gid", "pw_gecos", "pw_dir", "pw_shell")
    }
}

internal interface Crypt : Library {
    fun crypt(key: String?, salt: String?): String?

    companion object {
        val INSTANCE: Crypt = Native.load("crypt", Crypt::class.java)
    }
}

internal interface CLib : Library {
    fun getpwuid(uid: Int): Password?

    companion object {
        val INSTANCE: CLib = Native.load("c", CLib::class.java)
    }
}

object LinuxUtils : NativeUtils() {
    val instance = this
    fun hasSharedLibrary(name: String): Boolean {
        return File("/lib64/$name").exists() || File("/lib/$name").exists()
                || File("/usr/lib64/$name").exists() || File("/usr/lib/$name").exists()
                || HostUtils.getCpuArchitecture() == HostArchitecture.X86_64 && File("/usr/lib/x86_64-linux-gnu/$name").exists()
                || HostUtils.getCpuArchitecture() == HostArchitecture.ARM64 && File("/usr/lib/aarch64-linux-gnu/$name").exists()
                || HostUtils.getCpuArchitecture() == HostArchitecture.ARM && File("/usr/lib/armv7-linux-gnu/$name").exists()
    }

    fun getPamModuleDirs(): Array<String> {
        return arrayOf("/lib/security/", "/lib64/security")
    }

    fun getPamConfigDir(): String {
        return "/etc/pam.d/"
    }

    override fun getAllUsers(): List<String> {
        val shell = Shell.getForPlatform()!!
        val shadowStr = shell.runCommand("cat /etc/passwd")
        if(shadowStr.exitCode != 0)
            throw Exception("Could not check for user password !")

        val userList = ArrayList<String>()
        val scanner = Scanner(shadowStr.output)
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            val lineSplit = line.split(':')
            if(lineSplit.size < 4)
                continue

            val uid = lineSplit[2].toInt()
            if(uid != 0 && (uid < 1000 || uid > 60000))
                continue

            val userName = lineSplit[0]
            userList.add(userName)
        }
        scanner.close()
        return userList
    }

    override fun getCurrentUserName(): String {
        var uid: Int? = null

        val sudoUid = System.getenv("SUDO_UID")
        if(sudoUid != null) {
            uid = sudoUid.toIntOrNull()
        }
        if(uid == null) {
            uid = LibC.INSTANCE.getuid()
        }

        val userName = CLib.INSTANCE.getpwuid(uid)
        return userName?.pw_name ?: throw Exception("Could not determine username !")
    }

    override fun checkUserLogin(userName: String, password: String): Boolean {
        val shell = Shell.getForPlatform()!!
        val shadowStr = shell.runCommand("cat /etc/shadow")
        if(shadowStr.exitCode != 0)
            throw Exception("Could not check for user password !")

        var encryptedPwd: String? = null
        val scanner = Scanner(shadowStr.output)
        while (scanner.hasNextLine()) {
            val line = scanner.nextLine()
            val lineSplit = line.split(':')
            if(lineSplit.size < 2)
                continue

            if(lineSplit[0] == userName) {
                encryptedPwd = lineSplit[1]
                break
            }
        }
        scanner.close()

        if(encryptedPwd == null)
            return false
        return encryptedPwd == Crypt.INSTANCE.crypt(password, encryptedPwd)
    }
}
package com.meisapps.pcbiounlock.shell

import com.meisapps.pcbiounlock.shell.linux.LinuxShell
import com.meisapps.pcbiounlock.shell.windows.WindowsShell
import com.meisapps.pcbiounlock.utils.host.OperatingSystem

class CommandResult {
    var exitCode = 0
    var output = ""
}

abstract class Shell {
    var hasShell = false

    companion object {
        private var shellObj: Shell? = null

        fun getForPlatform(): Shell? {
            if(shellObj != null)
                return shellObj

            shellObj = if(OperatingSystem.isLinux)
                           LinuxShell()
                       else if(OperatingSystem.isWindows)
                           WindowsShell()
                       else null
            return shellObj
        }
    }

    abstract fun isRunningAsAdmin(): Boolean
    abstract fun restartAsAdmin(args: Array<String>, classPath: String)

    abstract fun runUserCommand(cmd: String): CommandResult

    abstract fun acquire()
    abstract fun release()

    abstract fun runCommand(cmd: String): CommandResult

    abstract fun readBytes(path: String): ByteArray?
    abstract fun writeBytes(path: String, data: ByteArray)
}

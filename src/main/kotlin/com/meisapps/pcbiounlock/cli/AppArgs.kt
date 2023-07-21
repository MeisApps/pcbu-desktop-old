package com.meisapps.pcbiounlock.cli

import com.beust.jcommander.Parameter

class AppArgs {
    @Parameter(names = ["-h", "--help"], description = "Shows the help screen.", help = true)
    var showHelp = false

    @Parameter(names = ["-c", "--cli"], description = "Forces to run as a console app.")
    var runCli = false

    @Parameter(names = ["-s", "--setup"], description = "Forces to run the setup.")
    var runSetup = false

    @Parameter(names = ["--ip"], description = "Forces to use the specified IP address for pairing and unlocking.")
    var forcedIp: String? = null
}
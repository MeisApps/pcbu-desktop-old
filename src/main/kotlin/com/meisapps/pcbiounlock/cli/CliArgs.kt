package com.meisapps.pcbiounlock.cli

import com.beust.jcommander.Parameter

class CliArgs {
    @Parameter
    private var parameters = ArrayList<String>()

    @Parameter(names = ["-i", "--install"], description = "Installs the service module, reinstalling if already present.")
    var install = false

    @Parameter(names = ["-u", "--uninstall"], description = "Uninstalls the service module.")
    var uninstall = false

    @Parameter(names = ["-p", "--pair"], description = "Shows the help screen.")
    var forcePair = false
}
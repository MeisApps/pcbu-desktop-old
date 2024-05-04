package com.meisapps.pcbiounlock.updater

import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.utils.VersionInfo
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.io.RestClient
import com.meisapps.pcbiounlock.utils.text.I18n
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.awt.Desktop
import java.awt.EventQueue
import java.net.URI
import javax.swing.JOptionPane

object AppUpdater {
    private fun isUpdateAvailable(): Boolean {
        val response = RestClient.get("https://api.meis-apps.com/rest/appUpdater/checkUpdates", mapOf("product" to "PCBioUnlock", "platform" to "Desktop"))
            ?: return false
        val version = response.jsonObject["version"]?.jsonPrimitive?.content
            ?: return false
        return VersionInfo.compareVersion(VersionInfo.getAppVersion(), version) == 1
    }

    fun checkForUpdates(runCli: Boolean) {
        if(!isUpdateAvailable()) {
            Console.println("No update available.")
            return
        }

        if(runCli) {
            Console.println("An update is available.")
            Console.println("Please download it from https://meis-apps.com/pcbu")
        } else {
            EventQueue.invokeAndWait {
                val dialogResult = JOptionPane.showConfirmDialog(
                    null,
                    I18n.get("ui_update_available"),
                    I18n.get("info"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
                )
                if (dialogResult == JOptionPane.YES_OPTION) {
                    val link = "https://meis-apps.com/pcbu"
                    try {
                        Desktop.getDesktop().browse(URI(link))
                    } catch (e: UnsupportedOperationException) {
                        JOptionPane.showMessageDialog(null, "Could not open browser. Please download it from https://meis-apps.com/pcbu", "Error", JOptionPane.ERROR_MESSAGE)
                    }
                }
            }
        }
    }

    fun updateNatives(runCli: Boolean) {
        val serviceInstaller = ServiceInstaller.getForPlatform(Shell.getForPlatform()!!)!!
        if(VersionInfo.compareVersion(AppSettings.get().installedVersion, VersionInfo.getAppVersion()) == 1) {
            if(runCli) {
                Console.println("Updating to version ${VersionInfo.getAppVersion()}...")
                serviceInstaller.reinstall()
                Console.println("Update complete.")
            } else {
                val updaterFrame = UpdaterFrame()
                EventQueue.invokeAndWait {
                    updaterFrame.updateAction(I18n.get("ui_installing_update"))
                    updaterFrame.isVisible = true
                }

                serviceInstaller.reinstall()
                Thread.sleep(200) // Avoid race condition

                EventQueue.invokeAndWait {
                    updaterFrame.isVisible = false
                }
            }
        }
    }
}

package com.meisapps.pcbiounlock.ui.panels.settings

import com.google.common.net.InetAddresses
import com.meisapps.pcbiounlock.service.api.PCBUApi
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.storage.PCBUAppSettings
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.ui.panels.NamedPanel
import com.meisapps.pcbiounlock.ui.panels.Panel
import com.meisapps.pcbiounlock.utils.host.HostOS
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.stream.IntStream
import javax.swing.*


abstract class SettingsPanel(hasPlatformSettings: Boolean) : Panel {
    companion object {
        fun getForPlatform(isSetup: Boolean): SettingsPanel {
            return when(OperatingSystem.get()) {
                HostOS.LINUX -> LinuxSettingsPanel(isSetup)
                else -> CommonSettingsPanel()
            }
        }
    }

    private val ipAutoChkBox = JCheckBox(I18n.get("ui_auto_detect"))
    private val ipInput = JTextField()

    private val unlockPortInput = JTextField()
    private val pairingPortInput = JTextField()

    private val langSelectBox = JComboBox(I18n.languages.stream().map { it.name }.toArray())

    private val settingsRootPanel = JPanel()

    init {
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(5, 20, 5, 20)
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        settingsRootPanel.layout = GridBagLayout()

        val appSettingsPanel = NamedPanel(I18n.get("ui_app_settings"))
        appSettingsPanel.innerPanel.layout = GridBagLayout()
        val platformSettingsPanel = NamedPanel("${OperatingSystem.getString()} ${I18n.get("ui_settings")}")

        val ipAddrLbl = JLabel(I18n.get("ui_ip_address"))
        val unlockPortLbl = JLabel(I18n.get("ui_unlock_server_port"))
        val pairingPortLbl = JLabel(I18n.get("ui_pairing_server_port"))
        val langLbl = JLabel(I18n.get("ui_language"))

        ipAddrLbl.font = ipAddrLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        unlockPortLbl.font = unlockPortLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        pairingPortLbl.font = pairingPortLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        langLbl.font = langLbl.font.deriveFont(UIGlobals.DefaultFontSize)

        ipInput.font = ipInput.font.deriveFont(UIGlobals.DefaultFontSize)
        unlockPortInput.font = unlockPortInput.font.deriveFont(UIGlobals.DefaultFontSize)
        pairingPortInput.font = pairingPortInput.font.deriveFont(UIGlobals.DefaultFontSize)

        ipAutoChkBox.font = ipAutoChkBox.font.deriveFont(UIGlobals.DefaultFontSize)
        ipAutoChkBox.addActionListener {
            if(ipAutoChkBox.isSelected) {
                ipInput.isEnabled = false
                ipInput.text = PCBUApi.getLocalIP()
            } else {
                ipInput.isEnabled = true
            }
        }

        langSelectBox.font = langSelectBox.font.deriveFont(UIGlobals.DefaultFontSize)

        gbc.gridheight = 2
        gbc.gridx = 0
        gbc.gridy = 0
        appSettingsPanel.innerPanel.add(ipAddrLbl, gbc)
        gbc.gridheight = 1
        gbc.gridx = 1
        gbc.gridy = 0
        appSettingsPanel.innerPanel.add(ipAutoChkBox, gbc)
        gbc.gridx = 1
        gbc.gridy = 1
        appSettingsPanel.innerPanel.add(ipInput, gbc)

        /*gbc.gridx = 0
        gbc.gridy = 2
        appSettingsPanel.innerPanel.add(unlockPortLbl, gbc)
        gbc.gridx = 1
        gbc.gridy = 2
        appSettingsPanel.innerPanel.add(unlockPortInput, gbc)*/

        gbc.gridx = 0
        gbc.gridy = 3
        appSettingsPanel.innerPanel.add(pairingPortLbl, gbc)
        gbc.gridx = 1
        gbc.gridy = 3
        appSettingsPanel.innerPanel.add(pairingPortInput, gbc)

        gbc.gridx = 0
        gbc.gridy = 4
        appSettingsPanel.innerPanel.add(langLbl, gbc)
        gbc.gridx = 1
        gbc.gridy = 4
        appSettingsPanel.innerPanel.add(langSelectBox, gbc)

        // Root
        gbc.gridx = 0
        gbc.gridy = 0
        settingsRootPanel.add(appSettingsPanel, gbc)
        if(hasPlatformSettings) {
            gbc.gridx = 1
            gbc.gridy = 0
            settingsRootPanel.add(platformSettingsPanel, gbc)
        }

        init(platformSettingsPanel.innerPanel)

        // Settings
        val settings = AppSettings.get()
        if(settings.serverIP.isEmpty() || settings.serverIP == "auto") {
            ipAutoChkBox.isSelected = true
            ipInput.isEnabled = false
            ipInput.text = PCBUApi.getLocalIP()
        } else {
            ipAutoChkBox.isSelected = false
            ipInput.isEnabled = true
            ipInput.text = settings.serverIP
        }

        unlockPortInput.text = settings.unlockServerPort.toString()
        pairingPortInput.text = settings.pairingServerPort.toString()

        val langIdx = IntStream.range(0, I18n.languages.size)
            .filter { I18n.languages[it].code == settings.language }
            .findFirst().orElse(-1)
        if(langIdx != -1) {
            langSelectBox.selectedIndex = langIdx
        }
    }

    abstract fun init(rootPanel: JPanel)

    open fun applySettings() {
        val ipStr = if(ipAutoChkBox.isSelected) "auto" else ipInput.text
        val unlockPort = unlockPortInput.text.toIntOrNull()
        val pairingPort = pairingPortInput.text.toIntOrNull()
        if(unlockPort == null || pairingPort == null
            || unlockPort < 0 || unlockPort > 65535
            || pairingPort < 0 || pairingPort > 65535)
            throw Exception(I18n.get("ui_port_error"))
        if(ipStr != "auto" && !InetAddresses.isInetAddress(ipStr) || ipStr == "auto" && !InetAddresses.isInetAddress(PCBUApi.getLocalIP()))
            throw Exception(I18n.get("ui_ip_error"))

        var lang = "auto"
        val langItem = I18n.languages.stream().filter { it.name == langSelectBox.selectedItem as String? }.findFirst()
        if(langItem.isPresent) {
            lang = langItem.get().code
        }

        val settings = PCBUAppSettings(AppSettings.get().installedVersion, lang, ipStr, unlockPort, pairingPort)
        AppSettings.set(settings)
    }

    override fun getRootPanel(): JPanel {
        return settingsRootPanel
    }
}
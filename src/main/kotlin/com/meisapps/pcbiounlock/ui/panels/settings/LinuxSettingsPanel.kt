package com.meisapps.pcbiounlock.ui.panels.settings

import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.service.linux.LinuxServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JCheckBox
import javax.swing.JLabel
import javax.swing.JPanel

class LinuxSettingsPanel : SettingsPanel(true) {
    private lateinit var descLbl: JLabel

    lateinit var enableSudoChkBox: JCheckBox
    lateinit var enablePolkitChkBox: JCheckBox
    lateinit var enableLoginScreenChkBox: JCheckBox

    override fun init(rootPanel: JPanel) {
        val shell = Shell.getForPlatform()!!
        val serviceInstaller = ServiceInstaller.getForPlatform(shell)!! as LinuxServiceInstaller

        rootPanel.layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(5, 10, 5, 10)
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        descLbl = JLabel(I18n.get("ui_linux_integrations"))
        descLbl.font = descLbl.font.deriveFont(UIGlobals.DefaultFontSize)

        enableSudoChkBox = JCheckBox(I18n.get("ui_linux_integration_sudo"))
        enableSudoChkBox.font = enableSudoChkBox.font.deriveFont(UIGlobals.DefaultFontSize)
        enableSudoChkBox.isSelected = serviceInstaller.isSudoEnabled()

        enablePolkitChkBox = JCheckBox(I18n.get("ui_linux_integration_polkit"))
        enablePolkitChkBox.font = enablePolkitChkBox.font.deriveFont(UIGlobals.DefaultFontSize)
        enablePolkitChkBox.isSelected = serviceInstaller.isPolkitEnabled()

        enableLoginScreenChkBox = JCheckBox(I18n.get("ui_linux_integration_login"))
        enableLoginScreenChkBox.font = enableLoginScreenChkBox.font.deriveFont(UIGlobals.DefaultFontSize)
        enableLoginScreenChkBox.isSelected = serviceInstaller.isLoginManagerEnabled()
        enableLoginScreenChkBox.isEnabled = serviceInstaller.isGdmInstalled() ||
                                            serviceInstaller.isKdeInstalled() ||
                                            serviceInstaller.isSddmInstalled() ||
                                            serviceInstaller.isLightdmInstalled() ||
                                            serviceInstaller.isCinnamonInstalled()

        gbc.gridx = 0
        gbc.gridy = 0
        rootPanel.add(descLbl, gbc)

        gbc.gridx = 0
        gbc.gridy = 1
        rootPanel.add(enableSudoChkBox, gbc)
        gbc.gridx = 0
        gbc.gridy = 2
        rootPanel.add(enablePolkitChkBox, gbc)
        gbc.gridx = 0
        gbc.gridy = 3
        rootPanel.add(enableLoginScreenChkBox, gbc)
    }

    override fun applySettings() {
        super.applySettings()

        val shell = Shell.getForPlatform()!!
        val serviceInstaller = ServiceInstaller.getForPlatform(shell)!! as LinuxServiceInstaller

        serviceInstaller.setSudoEnabled(enableSudoChkBox.isSelected)
        serviceInstaller.setPolkitEnabled(enablePolkitChkBox.isSelected)
        serviceInstaller.setLoginManagerEnabled(enableLoginScreenChkBox.isSelected)
    }
}

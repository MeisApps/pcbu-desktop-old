package com.meisapps.pcbiounlock.ui.panels.pairing

import com.meisapps.pcbiounlock.server.pairing.UserData
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.natives.NativeUtils
import com.meisapps.pcbiounlock.utils.extensions.withFontSize
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*


class PairingUserPasswordPanel(form: IPairingForm, private val userName: String) : PairingPanel(form) {
    private val pwInput = JPasswordField()
    private val rootPanel = JPanel()

    private val native = NativeUtils.getForPlatform()

    init {
        rootPanel.layout = GridBagLayout()

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(10, 150, 10, 150)
        gbc.weightx = 0.0
        gbc.weighty = 0.0

        var displayName = userName
        if(OperatingSystem.isWindows) {
            val split = displayName.split('\\')
            displayName = split[1]
        }

        val winNoteLbl = JLabel(I18n.get("ui_note_microsoft_account")).withFontSize(UIGlobals.DefaultFontSize)
        val userLabel = JLabel("${I18n.get("ui_username")}:").withFontSize(UIGlobals.DefaultFontSize)
        val userNameLabel = JLabel(displayName).withFontSize(UIGlobals.DefaultFontSize)
        val pwLabel = JLabel("${I18n.get("ui_password")}:").withFontSize(UIGlobals.DefaultFontSize)

        if(OperatingSystem.isWindows) {
            gbc.insets = Insets(10, 150, 20, 150)
            gbc.gridwidth = 2
            gbc.gridx = 0
            gbc.gridy = 0
            rootPanel.add(winNoteLbl, gbc)
        }

        gbc.gridwidth = 1
        gbc.insets = Insets(10, 150, 10, 10)
        gbc.gridx = 0
        gbc.gridy = 1
        rootPanel.add(userLabel, gbc)
        gbc.gridx = 0
        gbc.gridy = 2
        rootPanel.add(pwLabel, gbc)

        gbc.insets = Insets(10, 10, 10, 150)
        gbc.weightx = 1.0
        gbc.gridx = 1
        gbc.gridy = 1
        rootPanel.add(userNameLabel, gbc)
        gbc.gridx = 1
        gbc.gridy = 2
        rootPanel.add(pwInput, gbc)
    }

    override fun initialize() {
        form.setDescription(I18n.get("ui_user_password_desc"))
        form.getFrame().rootPane.defaultButton = form.getNextButton()

        form.getNextButton().isVisible = true
        form.getNextButton().isEnabled = true
    }

    override fun onNextClicked(): PairingPanel? {
        if(!isValid()) {
            JOptionPane.showMessageDialog(form.getFrame(), I18n.get("ui_password_incorrect"), I18n.get("error"), JOptionPane.ERROR_MESSAGE)
            return null
        }

        val userData = UserData(userName, pwInput.password.concatToString())
        val selectPanel = PairingMethodSelectPanel(form, userData)

        form.getFrame().rootPane.defaultButton = null
        return selectPanel
    }

    override fun onBackClicked() {
        form.getFrame().rootPane.defaultButton = null
    }

    override fun getRootPanel(): JPanel {
        return rootPanel
    }

    private fun isValid(): Boolean {
        return native.checkUserLogin(userName, pwInput.password.concatToString())
    }
}
package com.meisapps.pcbiounlock.ui.main

import com.meisapps.pcbiounlock.MainFrame
import com.meisapps.pcbiounlock.ui.base.Form
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.ui.panels.settings.SettingsPanel
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class SettingsForm(mainFrame: MainFrame) : Form(mainFrame) {
    override fun createUI(contentPane: Container) {
        val rootLayout = GridBagLayout()
        contentPane.layout = rootLayout

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(30, 30, 30, 30)
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        val titleLbl = JLabel(I18n.get("ui_service_settings"))
        titleLbl.font = titleLbl.font.deriveFont(UIGlobals.TitleFontSize)

        val settingsPanel = SettingsPanel.getForPlatform()
        val saveBtn = JButton(I18n.get("ui_save"))
        saveBtn.font = saveBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        saveBtn.addActionListener {
            try {
                settingsPanel.applySettings()
                frame.displayForm(MainForm(frame as MainFrame))
            } catch(e: Exception) {
                JOptionPane.showMessageDialog(null, e.message, I18n.get("error"), JOptionPane.ERROR_MESSAGE)
            }
        }

        val abortBtn = JButton(I18n.get("ui_abort"))
        abortBtn.font = abortBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        abortBtn.addActionListener {
            frame.displayForm(MainForm(frame as MainFrame))
        }

        gbc.gridwidth = 2
        gbc.gridx = 0
        gbc.gridy = 0
        contentPane.add(titleLbl, gbc)

        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 1
        contentPane.add(settingsPanel.getRootPanel(), gbc)

        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        gbc.weighty = 0.0
        gbc.gridwidth = 1
        gbc.gridx = 0
        gbc.gridy = 2
        contentPane.add(abortBtn, gbc)
        gbc.anchor = GridBagConstraints.LINE_END
        gbc.gridx = 1
        gbc.gridy = 2
        contentPane.add(saveBtn, gbc)
    }

    override fun initialize() {
    }
}

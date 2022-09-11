package com.meisapps.pcbiounlock.updater

import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JProgressBar
import javax.swing.WindowConstants


class UpdaterFrame : JFrame() {
    private val descLbl = JLabel("Checking for updates...")

    private val progressBar = JProgressBar()
    private val progressLbl = JLabel("")

    init {
        // Init UI
        val rootLayout = GridBagLayout()
        contentPane.layout = rootLayout

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(10, 30, 10, 30)

        gbc.weightx = 1.0
        gbc.weighty = 0.0

        progressBar.isIndeterminate = true
        descLbl.font = descLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        progressLbl.font = progressLbl.font.deriveFont(UIGlobals.DefaultFontSize)

        gbc.gridx = 0
        gbc.gridy = 0
        contentPane.add(descLbl, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        contentPane.add(progressBar, gbc)
        gbc.fill = GridBagConstraints.NONE
        gbc.anchor = GridBagConstraints.LINE_END
        gbc.gridx = 0
        gbc.gridy = 2
        contentPane.add(progressLbl, gbc)

        pack()
        revalidate()

        title = "Updater"
        iconImage = ResourceHelper.getAppIcon()
        defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
        isResizable = false
        setLocationRelativeTo(null)
    }

    fun updateAction(action: String) {
        descLbl.text = action
    }
}
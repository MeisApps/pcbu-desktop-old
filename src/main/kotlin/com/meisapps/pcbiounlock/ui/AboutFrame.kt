package com.meisapps.pcbiounlock.ui

import com.meisapps.pcbiounlock.utils.VersionInfo
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.net.URI
import javax.swing.*

class AboutFrame() : JDialog() {
    companion object {
        const val MEIS_APPS_URL = "https://meis-apps.com"
    }

    init {
        // Init UI
        val rootLayout = GridBagLayout()
        contentPane.layout = rootLayout

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(20, 20, 0, 20)
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        val titleLbl = JLabel("PC Bio Unlock")
        titleLbl.font = titleLbl.font.deriveFont(28F)

        val versionLbl = JLabel("Version ${VersionInfo.getAppVersion()}")
        val byLbl = JLabel(I18n.get("ui_about_by"))

        val linkLbl = JLabel("<html><a href=>meis-apps.com</a>")
        linkLbl.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        linkLbl.addMouseListener(object : MouseListener {
            override fun mouseClicked(p0: MouseEvent?) {
                Desktop.getDesktop().browse(URI.create(MEIS_APPS_URL));
            }

            override fun mousePressed(p0: MouseEvent?) {}
            override fun mouseReleased(p0: MouseEvent?) {}
            override fun mouseEntered(p0: MouseEvent?) {}
            override fun mouseExited(p0: MouseEvent?) {}
        })
        val licencesLbl = JLabel(I18n.get("ui_about_licenses"))

        val licencesTextArea = JTextArea()
        licencesTextArea.isEditable = false

        val licenceText = ResourceHelper.getFileBytes("licenses.txt")!!.toString(Charsets.UTF_8)
        licencesTextArea.text = licenceText

        val licencesView = JScrollPane(licencesTextArea)
        SwingUtilities.invokeLater { licencesView.verticalScrollBar.value = 0 }

        gbc.gridx = 0
        gbc.gridy = 0
        contentPane.add(titleLbl, gbc)
        gbc.insets = Insets(10, 20, 0, 20)
        gbc.gridx = 0
        gbc.gridy = 1
        contentPane.add(versionLbl, gbc)
        gbc.insets = Insets(0, 20, 0, 20)
        gbc.gridx = 0
        gbc.gridy = 2
        contentPane.add(byLbl, gbc)
        gbc.insets = Insets(0, 20, 0, 20)
        gbc.gridx = 0
        gbc.gridy = 3
        contentPane.add(linkLbl, gbc)

        gbc.insets = Insets(20, 20, 0, 20)
        gbc.gridx = 0
        gbc.gridy = 4
        contentPane.add(licencesLbl, gbc)
        gbc.insets = Insets(5, 20, 20, 20)
        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 5
        contentPane.add(licencesView, gbc)

        pack()
        revalidate()

        title = I18n.get("ui_about")
        defaultCloseOperation = HIDE_ON_CLOSE
        isModal = true
        minimumSize = Dimension(640, 480)
        size = Dimension(640, 480)
        setLocationRelativeTo(null)
    }
}
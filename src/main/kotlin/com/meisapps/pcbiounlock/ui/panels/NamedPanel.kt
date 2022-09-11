package com.meisapps.pcbiounlock.ui.panels

import com.meisapps.pcbiounlock.ui.UIGlobals
import java.awt.*
import javax.swing.*

class NamedPanel(name: String) : JPanel() {
    val innerPanel = JPanel()
    val nameLbl = JLabel(name)

    init {
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        layout = GridBagLayout()
        nameLbl.font = nameLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        nameLbl.foreground = Color(160, 160, 160)
        nameLbl.alignmentX = Component.LEFT_ALIGNMENT

        gbc.gridx = 0
        gbc.gridy = 0
        add(nameLbl, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        add(Box.createRigidArea(Dimension(0, 5)), gbc)

        innerPanel.border = BorderFactory.createLineBorder(Color.lightGray)
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 2
        add(innerPanel, gbc)
    }
}
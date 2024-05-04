package com.meisapps.pcbiounlock.ui.main

import com.meisapps.pcbiounlock.MainFrame
import com.meisapps.pcbiounlock.ui.base.Form
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.ui.panels.pairing.*
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.Container
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.util.*
import javax.swing.*

class PairingForm(mainFrame: MainFrame) : Form(mainFrame), IPairingForm {
    private lateinit var contentPanel: Container

    private lateinit var currentPanel: PairingPanel
    private val panelStack = Stack<PairingPanel>()

    private val descLbl = JLabel()
    private val nextBtn = JButton(I18n.get("ui_next"))

    override fun createUI(contentPane: Container) {
        val rootLayout = GridBagLayout()
        contentPane.layout = rootLayout
        contentPanel = contentPane

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(30, 30, 30, 30)
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        currentPanel = PairingUserSelectPanel(this)
        currentPanel.initialize()
        panelStack.add(currentPanel)

        val titleLbl = JLabel(I18n.get("ui_pairing"))
        titleLbl.font = titleLbl.font.deriveFont(UIGlobals.TitleFontSize)
        descLbl.font = descLbl.font.deriveFont(UIGlobals.DescFontSize)

        val backBtn = JButton(I18n.get("ui_back"))
        backBtn.font = nextBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        backBtn.addActionListener {
            currentPanel.onBackClicked()
            if(!panelStack.empty())
                panelStack.pop()

            val prevPanel = if(panelStack.empty()) null else panelStack.peek()
            if(prevPanel != null) {
                updatePanel(prevPanel)
            } else {
                frame.displayForm(MainForm(frame as MainFrame))
            }
        }

        nextBtn.font = nextBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        nextBtn.addActionListener {
            val panel = currentPanel.onNextClicked()
            if(panel != null) {
                updatePanel(panel)
                panelStack.add(panel)
            } else if(currentPanel is QRScanPanel) {
                currentPanel.onBackClicked()
                frame.displayForm(MainForm(frame as MainFrame))
            }
        }

        gbc.gridwidth = 2
        gbc.gridx = 0
        gbc.gridy = 0
        contentPane.add(titleLbl, gbc)

        gbc.insets = Insets(0, 30, 0, 30)
        gbc.gridx = 0
        gbc.gridy = 1
        contentPane.add(descLbl, gbc)

        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 2
        contentPane.add(currentPanel.getRootPanel(), gbc)

        gbc.fill = GridBagConstraints.NONE
        gbc.insets = Insets(30, 30, 30, 30)
        gbc.gridwidth = 1
        gbc.weightx = 0.0
        gbc.weighty = 0.0
        gbc.gridx = 0
        gbc.gridy = 3
        contentPane.add(backBtn, gbc)
        gbc.anchor = GridBagConstraints.LINE_END
        gbc.gridx = 1
        gbc.gridy = 3
        contentPane.add(nextBtn, gbc)
    }

    override fun initialize() {
    }

    override fun getFrame(): JFrame {
        return frame
    }

    override fun getNextButton(): JButton {
        return nextBtn
    }

    override fun setDescription(message: String) {
        descLbl.text = message
    }

    override fun onDevicePaired() {
        frame.displayForm(MainForm(frame as MainFrame))
    }

    private fun updatePanel(panel: PairingPanel) {
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(30, 30, 30, 30)
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.gridwidth = 4
        gbc.gridx = 0
        gbc.gridy = 2

        contentPanel.remove(currentPanel.getRootPanel())
        contentPanel.add(panel.getRootPanel(), gbc)
        frame.redrawUI()

        currentPanel = panel
        currentPanel.initialize()
        nextBtn.text = if(currentPanel is QRScanPanel) I18n.get("ui_main_menu") else I18n.get("ui_next")
    }
}

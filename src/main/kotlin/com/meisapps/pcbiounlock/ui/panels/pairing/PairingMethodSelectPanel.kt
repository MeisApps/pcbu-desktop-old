package com.meisapps.pcbiounlock.ui.panels.pairing

import com.meisapps.pcbiounlock.server.pairing.PairingMethod
import com.meisapps.pcbiounlock.server.pairing.UserData
import com.meisapps.pcbiounlock.service.api.BluetoothApi
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.ButtonGroup
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JRadioButton

class PairingMethodSelectPanel(form: IPairingForm, private val userData: UserData) : PairingPanel(form) {
    private var method = PairingMethod.TCP
    private val rootPanel = JPanel()

    init {
        rootPanel.layout = GridBagLayout()

        val tcpRadioButton = JRadioButton(I18n.get("ui_pairing_method_tcp"))
        val btRadioButton = JRadioButton(I18n.get("ui_pairing_method_bt"))
        val cloudRadioButton = JRadioButton(I18n.get("ui_pairing_method_cloud"))

        val tcpDescLbl = JLabel(I18n.get("ui_pairing_method_tcp_desc"))
        val btDescLbl = JLabel(I18n.get("ui_pairing_method_bt_desc"))
        val cloudDescLbl = JLabel(I18n.get("ui_pairing_method_cloud_desc"))

        tcpDescLbl.font = tcpDescLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        btDescLbl.font = btDescLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        tcpRadioButton.font = tcpRadioButton.font.deriveFont(UIGlobals.DefaultFontSize)
        btRadioButton.font = btRadioButton.font.deriveFont(UIGlobals.DefaultFontSize)

        tcpRadioButton.addActionListener {
            method = PairingMethod.TCP
        }
        btRadioButton.addActionListener {
            method = PairingMethod.BLUETOOTH
        }
        btRadioButton.isEnabled = BluetoothApi.isBluetoothAvailable()
        cloudRadioButton.addActionListener {
            method = PairingMethod.CLOUD_TCP
        }

        val group = ButtonGroup()
        group.add(tcpRadioButton)
        group.add(btRadioButton)
        group.add(cloudRadioButton)
        group.setSelected(tcpRadioButton.model, true)

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        gbc.insets = Insets(5, 30, 5, 30)
        gbc.gridx = 0
        gbc.gridy = 0
        rootPanel.add(tcpRadioButton, gbc)
        gbc.insets = Insets(5, 30, 40, 30)
        gbc.gridx = 0
        gbc.gridy = 1
        rootPanel.add(tcpDescLbl, gbc)

        gbc.insets = Insets(5, 30, 5, 30)
        gbc.gridx = 0
        gbc.gridy = 2
        rootPanel.add(btRadioButton, gbc)
        gbc.insets = Insets(5, 30, 40, 30)
        gbc.gridx = 0
        gbc.gridy = 3
        rootPanel.add(btDescLbl, gbc)

        /*gbc.insets = Insets(5, 30, 5, 30)
        gbc.gridx = 0
        gbc.gridy = 4
        rootPanel.add(cloudRadioButton, gbc)
        gbc.gridx = 0
        gbc.gridy = 5
        rootPanel.add(cloudDescLbl, gbc)*/
    }

    override fun initialize() {
        form.setDescription(I18n.get("ui_pairing_select_method"))
    }

    override fun onNextClicked(): PairingPanel {
        return if(method == PairingMethod.BLUETOOTH) {
            form.getNextButton().isEnabled = false
            BluetoothScanPanel(form, userData)
        } else {
            QRScanPanel(form, method, userData)
        }
    }

    override fun onBackClicked() {
    }

    override fun getRootPanel(): JPanel {
        return rootPanel
    }
}

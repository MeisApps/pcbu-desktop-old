package com.meisapps.pcbiounlock.ui.panels.pairing

import com.meisapps.pcbiounlock.server.pairing.PairingMethod
import com.meisapps.pcbiounlock.server.pairing.UserData
import com.meisapps.pcbiounlock.service.api.BluetoothApi
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.*
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JProgressBar

class BluetoothPairPanel(form: IPairingForm, private val userData: UserData, private val selectedDevice: BluetoothApi.BluetoothDevice) : PairingPanel(form) {
    private val rootPanel = JPanel()
    private val progressBar = JProgressBar(0, 100)

    private var pairThread: Thread? = null

    init {
        rootPanel.layout = GridBagLayout()
        val progressContainer = JPanel()
        progressContainer.layout = GridBagLayout()
        progressContainer.alignmentX = Component.LEFT_ALIGNMENT
        progressBar.isIndeterminate = true

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(5, 10, 5, 10)
        gbc.weightx = 1.0
        gbc.gridx = 0
        gbc.gridy = 0
        progressContainer.add(progressBar, gbc)

        // Root
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 0
        rootPanel.add(progressContainer, gbc)
    }

    override fun initialize() {
        pairThread = Thread {
            val result = BluetoothApi.pairDevice(selectedDevice)
            EventQueue.invokeAndWait {
                if(result) {
                    form.getNextButton().isEnabled = true
                    form.getNextButton().doClick()
                } else {
                    JOptionPane.showMessageDialog(form.getFrame(), I18n.get("ui_pairing_bluetooth_pair_error"), I18n.get("error"), JOptionPane.ERROR_MESSAGE)
                    form.getBackButton().isEnabled = true
                    form.getBackButton().doClick()
                }
            }
        }
        pairThread!!.start()
        form.setDescription(I18n.get("ui_pairing_bluetooth_wait_pair"))
        form.getNextButton().isEnabled = false
        form.getBackButton().isEnabled = false
    }

    override fun onNextClicked(): PairingPanel? {
        return QRScanPanel(form, PairingMethod.BLUETOOTH, userData, selectedDevice.address)
    }

    override fun onBackClicked() {}

    override fun getRootPanel(): JPanel {
        return rootPanel
    }
}

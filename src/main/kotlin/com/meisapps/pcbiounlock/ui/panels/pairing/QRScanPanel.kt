package com.meisapps.pcbiounlock.ui.panels.pairing

import com.meisapps.pcbiounlock.server.pairing.PairingMethod
import com.meisapps.pcbiounlock.server.pairing.PairingServer
import com.meisapps.pcbiounlock.server.pairing.UserData
import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.ui.base.FormFrame
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import com.meisapps.pcbiounlock.utils.text.QRGenerator
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel

class QRScanPanel(form: IPairingForm, method: PairingMethod, userData: UserData, btAddr: String = "") : PairingPanel(form) {
    private val server = PairingServer(DeviceStorage.getForPlatform(Shell.getForPlatform()!!)!!, method, userData)
    private val rootPanel = JPanel()

    init {
        rootPanel.layout = GridBagLayout()
        server.bluetoothAddress = btAddr
        server.setOnDevicePairedListener {
            Thread {
                server.stop()
            }.start()
            form.onDevicePaired()
        }

        val qrImageData = QRGenerator.getQRCodeImage(server.getQRText(), 400, 400)
        val qrImage = ImageIO.read(ByteArrayInputStream(qrImageData))
        val qrLabel = JLabel(ImageIcon(qrImage))

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(0, 0, 0, 0)
        gbc.weightx = 1.0
        gbc.weighty = 1.0

        gbc.gridx = 0
        gbc.gridy = 0
        rootPanel.add(qrLabel, gbc)
    }

    override fun initialize() {
        form.setDescription(I18n.get("ui_pairing_qr_scan"))
        try {
            server.start()
        } catch (e: Exception) {
            Console.println(e.stackTraceToString())
            JOptionPane.showMessageDialog(form.getFrame(), I18n.get("error_pairing_server"), I18n.get("error"), JOptionPane.ERROR_MESSAGE)
        }
    }

    override fun onNextClicked(): PairingPanel? {
        return null
    }

    override fun onBackClicked() {
        server.stop()
    }

    override fun getRootPanel(): JPanel {
        return rootPanel
    }
}

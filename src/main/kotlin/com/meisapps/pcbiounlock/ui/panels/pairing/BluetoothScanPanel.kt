package com.meisapps.pcbiounlock.ui.panels.pairing

import com.meisapps.pcbiounlock.server.pairing.PairingMethod
import com.meisapps.pcbiounlock.server.pairing.UserData
import com.meisapps.pcbiounlock.service.api.BluetoothApi
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.EventQueue
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.*

class BluetoothScanPanel(form: IPairingForm, private val userData: UserData) : PairingPanel(form) {
    private var selectedDevice: BluetoothApi.BluetoothDevice? = null

    private val rootPanel = JPanel()
    private val devicesList = JList<BluetoothApi.BluetoothDevice>(DefaultListModel())

    private lateinit var scanThread: Thread
    private var isRunning = false

    init {
        rootPanel.layout = GridBagLayout()
        val descLbl = JLabel(I18n.get("ui_pairing_bluetooth_scanning"))
        descLbl.font = descLbl.font.deriveFont(UIGlobals.DefaultFontSize)

        devicesList.font = devicesList.font.deriveFont(UIGlobals.DefaultFontSize)
        devicesList.selectionMode = DefaultListSelectionModel.SINGLE_SELECTION
        devicesList.addListSelectionListener {
            selectedDevice = if(it.firstIndex >= devicesList.model.size) null else devicesList.model.getElementAt(it.firstIndex)
            form.getNextButton().isEnabled = selectedDevice != null
        }

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(0, 0, 0, 0)
        gbc.weightx = 1.0
        gbc.weighty = 0.0

        gbc.insets = Insets(5, 0, 5, 0)
        gbc.gridx = 0
        gbc.gridy = 0
        rootPanel.add(descLbl, gbc)

        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 1
        rootPanel.add(devicesList, gbc)
    }

    override fun initialize() {
        form.setDescription(I18n.get("ui_pairing_bluetooth_select"))

        val model = devicesList.model as DefaultListModel
        model.clear()
        startScan()
    }

    override fun onNextClicked(): PairingPanel? {
        if (selectedDevice == null)
            return null

        stopScan()
        if(OperatingSystem.isWindows)
            return BluetoothPairPanel(form, userData, selectedDevice!!)
        return QRScanPanel(form, PairingMethod.BLUETOOTH, userData, selectedDevice!!.address)
    }

    override fun onBackClicked() {
        form.getNextButton().isEnabled = true
        stopScan()
    }

    override fun getRootPanel(): JPanel {
        return rootPanel
    }

    private fun startScan() {
        if(isRunning)
            return

        isRunning = true
        scanThread = Thread { scanThread() }
        scanThread.start()
    }

    private fun stopScan() {
        if(!isRunning)
            return

        // Do not wait here
        isRunning = false
    }

    private fun scanThread() {
        Console.println("Starting bluetooth scan...")
        try {
            while (isRunning) {
                val devices = BluetoothApi.scanForDevices()
                if(!isRunning)
                    break

                EventQueue.invokeAndWait {
                    val model = devicesList.model as DefaultListModel
                    model.clear()
                    for((idx, device) in devices.withIndex())
                        model.add(idx, device)
                }

                Thread.sleep(100)
            }
        } catch (e: Exception) {
            Console.println(e.stackTraceToString())
        }
        Console.println("Bluetooth scan stopped.")
    }
}

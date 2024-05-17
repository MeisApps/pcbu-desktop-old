package com.meisapps.pcbiounlock.ui.main

import com.meisapps.pcbiounlock.MainFrame
import com.meisapps.pcbiounlock.service.DeviceStorage
import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.storage.AppSettings
import com.meisapps.pcbiounlock.ui.AboutFrame
import com.meisapps.pcbiounlock.ui.LoadingForm
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.ui.base.Form
import com.meisapps.pcbiounlock.ui.panels.NamedPanel
import com.meisapps.pcbiounlock.utils.VersionInfo
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.*
import javax.swing.*
import javax.swing.table.DefaultTableModel


class MainForm(mainFrame: MainFrame) : Form(mainFrame) {
    private val shell = Shell.getForPlatform()!!
    private val serviceInstaller = ServiceInstaller.getForPlatform(shell)!!
    private val deviceStorage = DeviceStorage.getForPlatform(shell)!!

    private val installStatusLbl = JLabel()
    private val installVersionLbl = JLabel()

    private val pairStatusLbl = JLabel()
    private val pairDeviceLbl = JLabel()
    private val pairedDevicesTbl = JTable(DefaultTableModel(arrayOf(arrayOf()), arrayOf("ID", I18n.get("ui_device_name"), I18n.get("ui_user"), I18n.get("ui_method"))))

    private val reinstallBtn = JButton(I18n.get("ui_reinstall"))
    private val installBtn = JButton(I18n.get("ui_install"))
    private val pairingBtn = JButton(I18n.get("ui_pairing_pair"))
    private val pairingDeleteBtn = JButton(I18n.get("ui_pairing_delete"))

    override fun createUI(contentPane: Container) {
        val rootLayout = GridBagLayout()
        contentPane.layout = rootLayout

        val defaultInsets = Insets(10, 10, 10, 10)
        val buttonInsets = Insets(10, 10, 10, 10)

        val gbc = GridBagConstraints()
        gbc.insets = defaultInsets

        val titleLbl = JLabel("PC Bio Unlock")
        titleLbl.font = titleLbl.font.deriveFont(UIGlobals.TitleFontSize)
        titleLbl.alignmentX = Component.LEFT_ALIGNMENT

        // Install container
        val installPanel = NamedPanel(I18n.get("ui_service_module"))
        installPanel.innerPanel.layout = GridBagLayout()

        installStatusLbl.font = installStatusLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        installVersionLbl.font = installVersionLbl.font.deriveFont(UIGlobals.DefaultFontSize)

        installBtn.font = installBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        installBtn.addActionListener {
            val dialogResult = JOptionPane.showConfirmDialog(
                frame,
                I18n.get("ui_uninstall_confirm"),
                I18n.get("warning"),
                JOptionPane.YES_NO_OPTION
            )
            if (dialogResult == JOptionPane.YES_OPTION) {
                val loadForm = LoadingForm(frame as MainFrame, if(serviceInstaller.isInstalled()) I18n.get("ui_uninstalling") else I18n.get("ui_installing"))
                loadForm.setOnCompletionListener {
                    frame.displayForm(MainForm(frame))
                }
                loadForm.setOnErrorListener {
                    Console.println(it.stackTraceToString())

                    JOptionPane.showMessageDialog(frame, "Error: " + it.message, I18n.get("error"), JOptionPane.ERROR_MESSAGE)
                    frame.displayForm(MainForm(frame))
                }
                frame.displayForm(loadForm)

                loadForm.load {
                    if(serviceInstaller.isInstalled()) {
                        serviceInstaller.uninstall(true)
                    } else {
                        serviceInstaller.install()
                    }

                    JOptionPane.showMessageDialog(frame, I18n.get("ui_success"), I18n.get("info"), JOptionPane.INFORMATION_MESSAGE)
                    updateStatus()
                }
            }
        }

        reinstallBtn.font = reinstallBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        reinstallBtn.addActionListener {
            val loadForm = LoadingForm(frame as MainFrame, I18n.get("ui_reinstalling"))
            loadForm.setOnCompletionListener {
                frame.displayForm(MainForm(frame))
            }
            loadForm.setOnErrorListener {
                Console.println(it.stackTraceToString())

                JOptionPane.showMessageDialog(frame, "Error: " + it.message, I18n.get("error"), JOptionPane.ERROR_MESSAGE)
                frame.displayForm(MainForm(frame))
            }
            frame.displayForm(loadForm)

            loadForm.load {
                if(serviceInstaller.isInstalled()) {
                    serviceInstaller.uninstall(false)
                    serviceInstaller.install()
                }

                JOptionPane.showMessageDialog(frame, I18n.get("ui_success"), I18n.get("info"), JOptionPane.INFORMATION_MESSAGE)
                updateStatus()
            }
        }

        val settingsBtn = JButton(I18n.get("ui_settings"))
        settingsBtn.font = settingsBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        settingsBtn.addActionListener {
            frame.displayForm(SettingsForm(frame as MainFrame))
        }

        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER

        gbc.gridx = 0
        gbc.gridy = 0
        installPanel.innerPanel.add(installStatusLbl, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        installPanel.innerPanel.add(installVersionLbl, gbc)

        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.insets = buttonInsets

        gbc.gridx = 0
        gbc.gridy = 2
        installPanel.innerPanel.add(reinstallBtn, gbc)
        gbc.gridx = 0
        gbc.gridy = 3
        installPanel.innerPanel.add(installBtn, gbc)
        gbc.gridx = 0
        gbc.gridy = 4
        installPanel.innerPanel.add(settingsBtn, gbc)

        installPanel.alignmentX = Component.LEFT_ALIGNMENT
        installPanel.innerPanel.alignmentX = Component.LEFT_ALIGNMENT

        // Pair container
        val pairPanel = NamedPanel(I18n.get("ui_pairing"))
        pairPanel.innerPanel.layout = GridBagLayout()

        pairStatusLbl.font = pairStatusLbl.font.deriveFont(UIGlobals.DefaultFontSize)
        pairDeviceLbl.font = pairDeviceLbl.font.deriveFont(UIGlobals.DefaultFontSize)

        val pairedDevicesPanel = JPanel(GridLayout())
        pairedDevicesPanel.add(JScrollPane(pairedDevicesTbl))
        pairedDevicesTbl.columnModel.removeColumn(pairedDevicesTbl.columnModel.getColumn(0))
        pairedDevicesTbl.selectionModel.addListSelectionListener {
            pairingDeleteBtn.isEnabled = pairedDevicesTbl.selectedRow != -1
        }

        pairingBtn.font = pairingBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        pairingBtn.addActionListener {
            frame.displayForm(PairingForm(frame as MainFrame))
        }

        pairingDeleteBtn.isEnabled = false
        pairingDeleteBtn.font = pairingDeleteBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        pairingDeleteBtn.addActionListener {
            if(pairedDevicesTbl.selectedRow != -1) {
                val dialogResult = JOptionPane.showConfirmDialog(
                    frame,
                    I18n.get("ui_pairing_delete_confirm"),
                    I18n.get("warning"),
                    JOptionPane.YES_NO_OPTION
                )
                if (dialogResult == JOptionPane.YES_OPTION) {
                    val model = pairedDevicesTbl.model as DefaultTableModel
                    val selPairingId = model.getValueAt(pairedDevicesTbl.selectedRow, 0) as String
                    deviceStorage.removeDevice(selPairingId)

                    updateStatus()
                    JOptionPane.showMessageDialog(frame as MainFrame, I18n.get("ui_pairing_data_deleted"), I18n.get("info"), JOptionPane.INFORMATION_MESSAGE)
                }
            }
        }

        gbc.weightx = 0.0
        gbc.weighty = 0.0
        gbc.insets = defaultInsets

        gbc.gridx = 1
        gbc.gridy = 0
        pairPanel.innerPanel.add(pairStatusLbl, gbc)

        gbc.gridx = 1
        gbc.gridy = 1
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.gridwidth = 2
        pairPanel.innerPanel.add(pairedDevicesPanel, gbc)

        gbc.insets = buttonInsets
        gbc.gridwidth = 1
        gbc.weighty = 0.1
        gbc.gridx = 1
        gbc.gridy = 2
        pairPanel.innerPanel.add(pairingBtn, gbc)

        gbc.gridx = 2
        gbc.gridy = 2
        pairPanel.innerPanel.add(pairingDeleteBtn, gbc)

        pairPanel.alignmentX = Component.RIGHT_ALIGNMENT
        pairPanel.innerPanel.alignmentX = Component.RIGHT_ALIGNMENT
        pairPanel.nameLbl.alignmentX = Component.RIGHT_ALIGNMENT

        // Bottom panel
        val bottomPanel = JPanel()
        bottomPanel.layout = GridBagLayout()

        val aboutBtn = JButton(I18n.get("ui_about"))
        aboutBtn.font = aboutBtn.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        aboutBtn.addActionListener {
            val dialog = AboutFrame()
            dialog.isVisible = true
        }

        gbc.anchor = GridBagConstraints.LINE_END
        gbc.insets = defaultInsets
        gbc.weightx = 0.0
        gbc.weighty = 0.0
        gbc.gridx = 0
        gbc.gridy = 0
        bottomPanel.add(aboutBtn, gbc)

        val versionLbl = JLabel("Version " + VersionInfo.getAppVersion())
        versionLbl.font = versionLbl.font.deriveFont(UIGlobals.DefaultFontSize)

        gbc.gridx = 0
        gbc.gridy = 1
        bottomPanel.add(versionLbl, gbc)

        // Root
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(30, 30, 30, 30)

        gbc.gridwidth = 2
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        gbc.gridx = 0
        gbc.gridy = 0
        contentPane.add(titleLbl, gbc)

        gbc.insets = Insets(30, 30, 30, 30)
        installPanel.innerPanel.maximumSize = Dimension(400, 300)
        pairPanel.innerPanel.maximumSize = Dimension(400, 300)

        gbc.fill = GridBagConstraints.NONE
        gbc.ipadx = 150
        gbc.ipady = 250
        gbc.gridwidth = 1
        gbc.weightx = 1.0
        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 1
        contentPane.add(installPanel, gbc)

        gbc.anchor = GridBagConstraints.LINE_END
        gbc.gridx = 1
        gbc.gridy = 1
        contentPane.add(pairPanel, gbc)

        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_END
        gbc.ipadx = 0
        gbc.ipady = 0
        gbc.weightx = 1.0
        gbc.weighty = 0.0
        gbc.gridwidth = 2
        gbc.gridx = 0
        gbc.gridy = 2
        contentPane.add(bottomPanel, gbc)
        updateStatus()
    }

    override fun initialize() {
    }

    private fun updateStatus() {
        reinstallBtn.isVisible = serviceInstaller.isInstalled()
        installBtn.text = if(serviceInstaller.isInstalled()) I18n.get("ui_uninstall") else I18n.get("ui_install")
        pairingBtn.isEnabled = serviceInstaller.isInstalled()
        if(serviceInstaller.isInstalled()) {
            installStatusLbl.text = "${I18n.get("ui_status")}: ${I18n.get("ui_installed")}"
            installVersionLbl.text = "${I18n.get("ui_installed_version")}: ${AppSettings.get().installedVersion}"
        } else {
            installStatusLbl.text = "${I18n.get("ui_status")}: ${I18n.get("ui_not_installed")}"
            installVersionLbl.text = "${I18n.get("ui_installed_version")}: ${I18n.get("ui_none")}"
        }

        val pairedDevicesModel = pairedDevicesTbl.model as DefaultTableModel
        pairedDevicesModel.rowCount = 0
        if(deviceStorage.getDevices().isNotEmpty()) {
            pairStatusLbl.text = "${I18n.get("ui_status")}: ${I18n.get("ui_paired")}"
            for(device in deviceStorage.getDevices()) {
                pairedDevicesModel.addRow(arrayOf(device.pairingId, device.deviceName, device.userName, device.pairingMethod.toString()))
            }
        } else {
            pairStatusLbl.text = "${I18n.get("ui_status")}: ${I18n.get("ui_not_paired")}"
            pairDeviceLbl.text = "${I18n.get("ui_paired_device")}: ${I18n.get("ui_none")}"
        }
    }
}

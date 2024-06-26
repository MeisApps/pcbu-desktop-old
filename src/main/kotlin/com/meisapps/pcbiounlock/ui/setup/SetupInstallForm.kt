package com.meisapps.pcbiounlock.ui.setup

import com.meisapps.pcbiounlock.SetupFrame
import com.meisapps.pcbiounlock.ui.panels.settings.LinuxSettingsPanel
import com.meisapps.pcbiounlock.ui.panels.settings.SettingsPanel
import com.meisapps.pcbiounlock.utils.host.OperatingSystem
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.Container
import javax.swing.BoxLayout
import javax.swing.JOptionPane

class SetupInstallForm(frame: SetupFrame) : SetupStepForm(frame) {
    private val settingsPanel = SettingsPanel.getForPlatform()

    override fun createUI(contentPane: Container) {
        val rootLayout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        contentPane.layout = rootLayout
        contentPane.add(settingsPanel.getRootPanel())
    }

    override fun initialize() {
        if(settingsPanel is LinuxSettingsPanel) {
            settingsPanel.enableSudoChkBox.isSelected = true
            settingsPanel.enablePolkitChkBox.isSelected = true
            if(settingsPanel.enableLoginScreenChkBox.isEnabled)
                settingsPanel.enableLoginScreenChkBox.isSelected = true
        }
        if(OperatingSystem.isWindows) {
            settingsPanel.waitForKeyChkBox.isSelected = true
        }
    }

    override fun onNextClicked() {
        try {
            settingsPanel.applySettings()
            val setupFrame = frame as SetupFrame
            setupFrame.nextStep()
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(null, e.message, I18n.get("error"), JOptionPane.ERROR_MESSAGE)
        }
    }

    override fun onBackClicked() {
        val setupFrame = frame as SetupFrame
        setupFrame.prevStep()
    }
}

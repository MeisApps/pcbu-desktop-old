package com.meisapps.pcbiounlock.ui.setup

import com.meisapps.pcbiounlock.SetupFrame
import com.meisapps.pcbiounlock.ui.panels.settings.SettingsPanel
import java.awt.Container
import javax.swing.BoxLayout

class SetupInstallForm(frame: SetupFrame) : SetupStepForm(frame) {
    private val settingsPanel = SettingsPanel.getForPlatform(true)

    override fun createUI(contentPane: Container) {
        val rootLayout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        contentPane.layout = rootLayout
        contentPane.add(settingsPanel.getRootPanel())
    }

    override fun initialize() {
    }

    override fun onNextClicked() {
        val setupFrame = frame as SetupFrame
        setupFrame.setInstallSettings(settingsPanel)
        setupFrame.nextStep()
    }

    override fun onBackClicked() {
        val setupFrame = frame as SetupFrame
        setupFrame.prevStep()
    }
}
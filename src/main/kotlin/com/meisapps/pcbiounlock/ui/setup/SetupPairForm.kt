package com.meisapps.pcbiounlock.ui.setup

import com.meisapps.pcbiounlock.SetupFrame
import com.meisapps.pcbiounlock.ui.panels.pairing.*
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.Container
import java.util.*
import javax.swing.*


class SetupPairForm(frame: SetupFrame) : SetupStepForm(frame), IPairingForm {
    private lateinit var contentPanel: Container

    private lateinit var currentPanel: PairingPanel
    private val panelStack = Stack<PairingPanel>()

    override fun createUI(contentPane: Container) {
        val rootLayout = BoxLayout(contentPane, BoxLayout.Y_AXIS)
        contentPane.layout = rootLayout
        contentPanel = contentPane

        currentPanel = PairingUserSelectPanel(this)
        currentPanel.initialize()
        panelStack.add(currentPanel)

        contentPane.add(currentPanel.getRootPanel())
        getNextButton().isEnabled = true
    }

    override fun initialize() {
    }

    override fun onDevicePaired() {
        val setupFrame = frame as SetupFrame
        setupFrame.nextStep()
    }

    override fun onNextClicked() {
        val panel = currentPanel.onNextClicked()
        if(panel != null) {
            updatePanel(panel)
            panelStack.add(panel)
        }
    }

    override fun onBackClicked() {
        val setupFrame = frame as SetupFrame
        currentPanel.onBackClicked()
        if(!panelStack.empty())
            panelStack.pop()

        val prevPanel = if(panelStack.empty()) null else panelStack.peek()
        if(prevPanel != null) {
            updatePanel(prevPanel)
        } else {
            if(!setupFrame.prevStep())
                panelStack.add(currentPanel)
        }
    }

    override fun getFrame(): JFrame {
        return frame
    }

    override fun getNextButton(): JButton {
        return (frame as SetupFrame).btnNext
    }

    override fun getBackButton(): JButton {
        return (frame as SetupFrame).btnBack
    }

    override fun setDescription(message: String) {
        (frame as SetupFrame).lblSetupDesc.text = message
    }

    private fun updatePanel(panel: PairingPanel) {
        val setupFrame = frame as SetupFrame
        contentPanel.remove(currentPanel.getRootPanel())
        contentPanel.add(panel.getRootPanel())

        currentPanel = panel
        currentPanel.initialize()
        setupFrame.redrawUI()

        setupFrame.btnBack.text = if(currentPanel is PairingUserPasswordPanel) I18n.get("ui_abort") else I18n.get("ui_back")
        if(currentPanel is QRScanPanel) {
            getNextButton().isEnabled = false
        }
    }
}

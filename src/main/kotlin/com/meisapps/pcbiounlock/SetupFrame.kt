package com.meisapps.pcbiounlock

import com.meisapps.pcbiounlock.service.ServiceInstaller
import com.meisapps.pcbiounlock.shell.Shell
import com.meisapps.pcbiounlock.ui.base.Form
import com.meisapps.pcbiounlock.ui.LoadingForm
import com.meisapps.pcbiounlock.ui.UIGlobals
import com.meisapps.pcbiounlock.ui.base.FormFrame
import com.meisapps.pcbiounlock.ui.panels.settings.SettingsPanel
import com.meisapps.pcbiounlock.ui.setup.SetupInstallForm
import com.meisapps.pcbiounlock.ui.setup.SetupPairForm
import com.meisapps.pcbiounlock.ui.setup.SetupStepForm
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.*
import java.awt.event.WindowEvent
import java.awt.event.WindowListener
import javax.swing.*
import kotlin.system.exitProcess


enum class SetupStep {
    INTRO,
    INSTALL,
    INSTALL_PROGRESS,
    PAIR,
    COMPLETE,
    ERROR
}

class SetupFrame : FormFrame() {
    private val setupLbl = JLabel("Setup")
    val lblSetupDesc = JLabel()
    val btnBack = JButton()
    val btnNext = JButton()

    private val setupStepContainer = JPanel()
    private var setupStep = SetupStep.INTRO
    private var currentForm: Form? = null

    private var errorMessage = ""

    init {
        // Init UI
        val rootLayout = GridBagLayout()
        contentPane.layout = rootLayout

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(30, 30, 30, 30)

        gbc.weightx = 1.0
        gbc.weighty = 0.0

        setupLbl.font = setupLbl.font.deriveFont(UIGlobals.TitleFontSize)
        lblSetupDesc.font = lblSetupDesc.font.deriveFont(UIGlobals.DescFontSize)

        gbc.gridwidth = 2
        gbc.gridx = 0
        gbc.gridy = 0
        contentPane.add(setupLbl, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        contentPane.add(lblSetupDesc, gbc)
        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = 2
        contentPane.add(setupStepContainer, gbc)

        gbc.weighty = 0.0
        gbc.gridwidth = 1
        gbc.fill = GridBagConstraints.NONE
        gbc.anchor = GridBagConstraints.LINE_START

        gbc.ipadx = 40
        gbc.ipady = 20
        gbc.gridx = 0
        gbc.gridy = 3
        contentPane.add(btnBack, gbc)
        gbc.anchor = GridBagConstraints.LINE_END
        gbc.gridx = 1
        gbc.gridy = 3
        contentPane.add(btnNext, gbc)

        pack()
        revalidate()

        // Listeners
        btnNext.font = btnNext.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        btnNext.addActionListener {
            if(currentForm != null && currentForm is SetupStepForm) {
                (currentForm as SetupStepForm).onNextClicked()
                return@addActionListener
            }

            nextStep()
        }
        btnBack.font = btnBack.font.deriveFont(UIGlobals.DefaultButtonFontSize)
        btnBack.addActionListener {
            if(currentForm != null) {
                (currentForm as SetupStepForm).onBackClicked()
                return@addActionListener
            }

            prevStep()
        }

        addWindowListener(object : WindowListener {
            override fun windowClosing(p0: WindowEvent?) {
                // Cancel
                val dialogResult = JOptionPane.showConfirmDialog(
                    this@SetupFrame,
                    I18n.get("ui_setup_exit_confirm"),
                    I18n.get("warning"),
                    JOptionPane.YES_NO_OPTION
                )
                if (dialogResult == JOptionPane.YES_OPTION) {
                    exitProcess(0)
                }
            }

            override fun windowOpened(p0: WindowEvent?) {}
            override fun windowClosed(p0: WindowEvent?) {}
            override fun windowIconified(p0: WindowEvent?) {}
            override fun windowDeiconified(p0: WindowEvent?) {}
            override fun windowActivated(p0: WindowEvent?) {}
            override fun windowDeactivated(p0: WindowEvent?) {}
        })

        switchToStep(SetupStep.INTRO)
        setSize(900, 800)
        minimumSize = Dimension(900, 800)

        title = "Setup"
        iconImage = ResourceHelper.getAppIcon()
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        setLocationRelativeTo(null)
    }

    fun prevStep(): Boolean {
        val prevStepIdx = SetupStep.values().indexOf(setupStep) - 1
        val prevStep = if(prevStepIdx < 0) null else SetupStep.values()[prevStepIdx]
        if(prevStep == null || setupStep == SetupStep.PAIR) {
            // Cancel
            val dialogResult = JOptionPane.showConfirmDialog(
                this,
                I18n.get("ui_setup_exit_confirm"),
                I18n.get("warning"),
                JOptionPane.YES_NO_OPTION
            )
            if (dialogResult == JOptionPane.YES_OPTION) {
                exitProcess(0)
            }
            return false
        }

        switchToStep(prevStep)
        return true
    }

    fun nextStep() {
        val nextStepIdx = SetupStep.values().indexOf(setupStep) + 1
        val nextStep = if(nextStepIdx > SetupStep.values().size - 2) null else SetupStep.values()[nextStepIdx]
        if(nextStep == null) {
            // Finish
            isVisible = false
            val mainFrame = MainFrame()
            mainFrame.isVisible = true
            return
        }

        switchToStep(nextStep)
    }

    private fun onError(e: Exception) {
        errorMessage = e.message!!
        Console.println(errorMessage)
        switchToStep(SetupStep.ERROR)
    }

    private fun switchToStep(step: SetupStep) {
        setupStepContainer.removeAll()
        currentForm = null

        btnBack.text = if(step == SetupStep.INTRO) I18n.get("ui_cancel") else I18n.get("ui_back")
        btnNext.text = if(step == SetupStep.COMPLETE || step == SetupStep.ERROR) I18n.get("ui_finish") else I18n.get("ui_next")

        setupLbl.text = if(step == SetupStep.INSTALL_PROGRESS) I18n.get("ui_installing") else "Setup"

        when(step) {
            SetupStep.INTRO -> {
                lblSetupDesc.text = I18n.get("ui_setup_intro_desc")
                btnBack.text = I18n.get("ui_cancel")
            }
            SetupStep.INSTALL -> {
                lblSetupDesc.text = I18n.get("ui_setup_install_desc")
                btnNext.text = I18n.get("ui_install")

                val installForm = SetupInstallForm(this)
                displayForm(installForm)
            }
            SetupStep.INSTALL_PROGRESS -> {
                lblSetupDesc.text = ""
                btnNext.isEnabled = false
                btnBack.isEnabled = false

                val loadForm = LoadingForm(this, I18n.get("ui_installing"), false)
                loadForm.setOnCompletionListener {
                    nextStep()
                }
                loadForm.setOnErrorListener { onError(it) }
                displayForm(loadForm)

                // Install
                loadForm.load {
                    val shell = Shell.getForPlatform()!!
                    val serviceInstaller = ServiceInstaller.getForPlatform(shell)!!
                    serviceInstaller.install()
                }
            }
            SetupStep.PAIR -> {
                lblSetupDesc.text = ""
                btnBack.text = I18n.get("ui_abort")
                btnNext.isEnabled = false
                btnBack.isEnabled = true

                val pairForm = SetupPairForm(this)
                displayForm(pairForm)
            }
            SetupStep.COMPLETE -> {
                lblSetupDesc.text = I18n.get("ui_setup_complete_desc")
                btnNext.isEnabled = true
                btnBack.isEnabled = false
            }
            SetupStep.ERROR -> {
                lblSetupDesc.text = "An error has occurred: $errorMessage"
                btnNext.isEnabled = true
                btnBack.isEnabled = false
            }
        }

        setupStep = step
        revalidate()
        contentPane.repaint()
    }

    override fun displayForm(form: Form) {
        setupStepContainer.removeAll()
        form.createUI(setupStepContainer)
        revalidate()
        contentPane.repaint()

        form.initialize()
        currentForm = form
    }
}

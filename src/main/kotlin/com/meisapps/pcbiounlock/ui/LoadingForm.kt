package com.meisapps.pcbiounlock.ui

import com.meisapps.pcbiounlock.ui.base.FormFrame
import com.meisapps.pcbiounlock.MainFrame
import com.meisapps.pcbiounlock.ui.base.Form
import com.meisapps.pcbiounlock.utils.io.Console
import com.meisapps.pcbiounlock.utils.text.I18n
import java.awt.*
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JProgressBar
import kotlin.system.exitProcess

class LoadingForm(frame: FormFrame, private val title: String, private val showTitle: Boolean = true) : Form(frame) {
    private val progressBar = JProgressBar(0, 100)

    private var completionListener: (() -> Unit)? = null
    private var errorListener: ((Exception) -> Unit)? = {
        Console.println(it.stackTraceToString())
        JOptionPane.showMessageDialog(frame as MainFrame, "Error: " + it.message, I18n.get("fatal_error"), JOptionPane.ERROR_MESSAGE)
        exitProcess(1)
    }

    private var loadThread: Thread? = null

    override fun createUI(contentPane: Container) {
        val rootLayout = GridBagLayout()
        contentPane.layout = rootLayout

        val titleLbl = JLabel(title)
        titleLbl.font = titleLbl.font.deriveFont(UIGlobals.TitleFontSize)
        titleLbl.alignmentX = Component.LEFT_ALIGNMENT

        // Progress container
        val progressContainer = JPanel()
        progressContainer.layout = GridBagLayout()
        progressContainer.alignmentX = Component.LEFT_ALIGNMENT

        val descLbl = JLabel(I18n.get("ui_load_wait"))
        descLbl.font = descLbl.font.deriveFont(UIGlobals.DescFontSize)
        progressBar.isIndeterminate = true

        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.CENTER
        gbc.insets = Insets(5, 0, 5, 0)
        gbc.weightx = 1.0

        gbc.gridx = 0
        gbc.gridy = 0
        progressContainer.add(descLbl, gbc)
        gbc.gridx = 0
        gbc.gridy = 1
        progressContainer.add(progressBar, gbc)

        // Root
        gbc.fill = GridBagConstraints.BOTH
        gbc.anchor = GridBagConstraints.LINE_START
        gbc.insets = Insets(30, 30, 30, 30)
        gbc.weightx = 1.0

        if(showTitle) {
            gbc.gridx = 0
            gbc.gridy = 0
            contentPane.add(titleLbl, gbc)
        }

        gbc.weighty = 1.0
        gbc.gridx = 0
        gbc.gridy = if(showTitle) 1 else 0
        contentPane.add(progressContainer, gbc)
    }

    override fun initialize() {
    }

    fun load(workLoad: () -> Unit) {
        loadThread?.join()
        loadThread = Thread {
            try {
                workLoad()
                completionListener?.invoke()
            } catch (e: Exception) {
                errorListener?.invoke(e)
            }
        }
        loadThread?.start()
    }

    fun setOnCompletionListener(u: () -> Unit) {
        completionListener = u
    }

    fun setOnErrorListener(u: (Exception) -> Unit) {
        errorListener = u
    }
}
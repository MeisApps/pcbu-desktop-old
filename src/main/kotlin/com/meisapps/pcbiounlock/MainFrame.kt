package com.meisapps.pcbiounlock

import com.meisapps.pcbiounlock.ui.base.Form
import com.meisapps.pcbiounlock.ui.base.FormFrame
import com.meisapps.pcbiounlock.ui.main.MainForm
import com.meisapps.pcbiounlock.utils.io.ResourceHelper
import java.awt.Dimension
import javax.swing.ImageIcon


class MainFrame : FormFrame() {
    init {
        // Init UI
        displayForm(MainForm(this))

        title = "PC Bio Unlock"
        iconImage = ResourceHelper.getAppIcon()
        defaultCloseOperation = EXIT_ON_CLOSE
        minimumSize = Dimension(840, 740)

        setLocationRelativeTo(null)
        size = minimumSize
    }

    override fun displayForm(form: Form) {
        contentPane.removeAll()
        form.createUI(contentPane)
        redrawUI()

        form.initialize()
    }
}
package com.meisapps.pcbiounlock.ui.base

import javax.swing.JFrame

abstract class FormFrame : JFrame() {
    abstract fun displayForm(form: Form)

    fun redrawUI() {
        revalidate()
        contentPane.repaint()
    }
}
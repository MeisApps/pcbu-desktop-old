package com.meisapps.pcbiounlock.ui.panels.pairing

import com.meisapps.pcbiounlock.ui.panels.Panel
import javax.swing.JButton
import javax.swing.JFrame

interface IPairingForm {
    fun getFrame(): JFrame
    fun getNextButton(): JButton
    fun getBackButton(): JButton
    fun setDescription(message: String)

    fun onDevicePaired()
}

abstract class PairingPanel(protected val form: IPairingForm) : Panel {
    abstract fun initialize()

    abstract fun onNextClicked(): PairingPanel?
    abstract fun onBackClicked()
}

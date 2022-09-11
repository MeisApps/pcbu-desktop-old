package com.meisapps.pcbiounlock.ui.base

import java.awt.Container

abstract class Form(protected val frame: FormFrame) {
    abstract fun createUI(contentPane: Container)
    abstract fun initialize()

    open fun onDestroy() {
    }
}
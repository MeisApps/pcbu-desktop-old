package com.meisapps.pcbiounlock.ui.setup

import com.meisapps.pcbiounlock.SetupFrame
import com.meisapps.pcbiounlock.ui.base.Form

abstract class SetupStepForm(setupFrame: SetupFrame) : Form(setupFrame) {
    abstract fun onNextClicked()
    abstract fun onBackClicked()
}
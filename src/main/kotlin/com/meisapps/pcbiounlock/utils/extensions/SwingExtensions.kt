package com.meisapps.pcbiounlock.utils.extensions

import javax.swing.JLabel


fun JLabel.withFontSize(size: Float): JLabel {
    font = font.deriveFont(size)
    return this
}
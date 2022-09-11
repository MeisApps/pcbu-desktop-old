package com.meisapps.pcbiounlock.utils.extensions

import java.awt.Image
import javax.swing.ImageIcon


fun ImageIcon.scale(width: Int, height: Int): ImageIcon {
    val img = image.getScaledInstance(width, height, Image.SCALE_SMOOTH)
    return ImageIcon(img)
}
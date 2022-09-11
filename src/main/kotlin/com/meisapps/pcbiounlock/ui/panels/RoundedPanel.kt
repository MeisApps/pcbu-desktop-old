package com.meisapps.pcbiounlock.ui.panels

import java.awt.*
import javax.swing.JPanel


open class RoundedPanel(radius: Int, bgColor: Color?) : JPanel() {
    protected var backgroundColor: Color? = bgColor
    protected var cornerRadius = radius

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val arcs = Dimension(cornerRadius, cornerRadius)
        val width = width
        val height = height
        val graphics = g as Graphics2D
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        //Draws the rounded panel with borders.
        if (backgroundColor != null) {
            graphics.color = backgroundColor
        } else {
            graphics.color = background
        }
        graphics.fillRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height) //paint background
        graphics.drawRoundRect(0, 0, width - 1, height - 1, arcs.width, arcs.height) //paint border
    }
}
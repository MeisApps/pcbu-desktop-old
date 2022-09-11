package com.meisapps.pcbiounlock.utils

import java.awt.Component
import java.awt.Container

object SwingUtils {
    fun setContainerEnabled(container: Container, isEnabled: Boolean) {
        container.isEnabled = isEnabled
        val components: Array<Component> = container.components
        for (component in components) {
            if (component is Container) {
                setContainerEnabled(component, isEnabled)
            }
            component.isEnabled = isEnabled
        }
    }
}
package org.jetbrains.research.testspark.display.utils

import java.awt.Dimension
import javax.swing.Icon
import javax.swing.JButton

object IconButtonCreator {
    /**
     * Creates a button with the specified icon.
     *
     * @param icon the icon to be displayed on the button
     * @return the created button
     */
    fun getButton(icon: Icon, tip: String): JButton {
        val button = JButton(icon)
        button.isOpaque = false
        button.isContentAreaFilled = false
        button.isBorderPainted = false
        button.toolTipText = tip
        val size = button.preferredSize.height
        button.preferredSize = Dimension(size, size)
        return button
    }
}

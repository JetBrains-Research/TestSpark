package org.jetbrains.research.testspark.actions.template

import javax.swing.JButton
import javax.swing.JPanel

interface PanelBuilder {
    /**
     * Returns a JPanel object containing the title panel.
     *
     * @return a JPanel object representing the title panel
     */
    fun getTitlePanel(): JPanel

    /**
     * Returns the middle panel containing main components.
     *
     * @return the middle panel as a JPanel.
     */
    fun getMiddlePanel(): JPanel

    /**
     * Retrieves the bottom panel containing the back and next buttons.
     *
     * @return The JPanel containing the back and next buttons.
     */
    fun getBottomPanel(): JPanel

    /**
     * Retrieves the back button.
     *
     * @return The back button.
     */
    fun getBackButton(): JButton

    /**
     * Retrieves the reference to the "OK" button.
     *
     * @return The reference to the "OK" button.
     */
    fun getFinishedButton(): JButton

    /**
     * Updates the settings state based on the selected values from the UI components.
     */
    fun applyUpdates()
}

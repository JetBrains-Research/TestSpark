package org.jetbrains.research.testspark.settings.template

interface SettingsComponent {
    /**
     * Add tool tips and default settings
     */
    fun stylizePanel()

    /**
     * Create the main panel for settings page
     */
    fun createSettingsPanel()

    /**
     * Adds listeners to the panel components
     */
    fun addListeners()

    /**
     * Init components
     */
    fun initComponent() {
        stylizePanel()
        createSettingsPanel()
        addListeners()
    }
}

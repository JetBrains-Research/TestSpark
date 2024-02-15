package org.jetbrains.research.testspark.helpers

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import org.jetbrains.research.testspark.bundles.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform
import org.jetbrains.research.testspark.tools.llm.generation.grazie.GraziePlatform
import org.jetbrains.research.testspark.tools.llm.generation.openai.OpenAIPlatform
import javax.swing.DefaultComboBoxModel
import javax.swing.JTextField
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * Checks if the Grazie class is loaded.
 * @return true if the Grazie class is loaded, false otherwise.
 */
private fun isGrazieClassLoaded(): Boolean {
    val className = "org.jetbrains.research.grazie.Request"
    return try {
        Class.forName(className)
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

/**
 * Updates the model selector based on the selected platform in the platform selector.
 * If the selected platform is "Grazie", the model selector is disabled and set to display only "GPT-4".
 * If the selected platform is not "Grazie", the model selector is updated with the available modules fetched asynchronously using llmUserTokenField and enables the okLlmButton.
 * If the modules fetch fails, the model selector is set to display the default modules and is disabled.
 *
 * This method runs on a separate thread using ApplicationManager.getApplication().executeOnPooledThread{}.
 */
private fun updateModelSelector(
    platformSelector: ComboBox<String>,
    modelSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
    llmPlatforms: List<LLMPlatform>,
) {
    val settingsState = SettingsApplicationService.getInstance().state!!

    for (index in settingsState.llmPlatforms.indices) {
        if (platformSelector.selectedItem!!.toString() == settingsState.llmPlatforms[index].name) {
            ApplicationManager.getApplication().executeOnPooledThread {
                val modules = settingsState.llmPlatforms[index].getModels(llmUserTokenField.text)
                modelSelector.model = DefaultComboBoxModel(modules)
                if (modules.contains(settingsState.llmPlatforms[index].model)) {
                    modelSelector.selectedItem = settingsState.llmPlatforms[index].model
                    llmPlatforms[index].model = settingsState.llmPlatforms[index].model
                }
                modelSelector.isEnabled = true
                if (modules.contentEquals(arrayOf(""))) modelSelector.isEnabled = false
            }
        }
    }
}

/**
 * Updates LlmUserTokenField based on the selected platform in the platformSelector ComboBox.
 *
 * @param platformSelector The ComboBox that allows the user to select a platform.
 * @param llmUserTokenField The JTextField that displays the user token for the selected platform.
 */
private fun updateLlmUserTokenField(
    platformSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
    llmPlatforms: List<LLMPlatform>,
) {
    val settingsState = SettingsApplicationService.getInstance().state!!
    for (index in settingsState.llmPlatforms.indices) {
        if (platformSelector.selectedItem!!.toString() == settingsState.llmPlatforms[index].name) {
            llmUserTokenField.text = settingsState.llmPlatforms[index].token
            llmPlatforms[index].token = llmUserTokenField.text
        }
    }
}

/**
 * Adds listeners to various components in the LLM panel.
 *
 * @param platformSelector The combo box for selecting the LLM platform.
 * @param modelSelector The combo box for selecting the LLM model.
 * @param llmUserTokenField The text field for entering the LLM user token.
 * @param llmPlatforms The list of LLM platforms.
 */
fun addLLMPanelListeners(
    platformSelector: ComboBox<String>,
    modelSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
    llmPlatforms: List<LLMPlatform>,
) {
    llmUserTokenField.document.addDocumentListener(object : DocumentListener {
        override fun insertUpdate(e: DocumentEvent?) {
            updateToken()
        }

        override fun removeUpdate(e: DocumentEvent?) {
            updateToken()
        }

        override fun changedUpdate(e: DocumentEvent?) {
            updateToken()
        }

        private fun updateToken() {
            for (llmPlatform in llmPlatforms) {
                if (platformSelector.selectedItem!!.toString() == llmPlatform.name) {
                    llmPlatform.token = llmUserTokenField.text
                }
            }
            updateModelSelector(platformSelector, modelSelector, llmUserTokenField, llmPlatforms)
        }
    })

    platformSelector.addItemListener {
        updateLlmUserTokenField(platformSelector, llmUserTokenField, llmPlatforms)
        updateModelSelector(platformSelector, modelSelector, llmUserTokenField, llmPlatforms)
    }

    modelSelector.addItemListener {
        for (llmPlatform in llmPlatforms) {
            if (platformSelector.selectedItem!!.toString() == llmPlatform.name) {
                llmPlatform.model = modelSelector.item
            }
        }
    }
}

/**
 * Stylizes the main components of the application.
 *
 * @param llmUserTokenField the text field for the LLM user token
 * @param modelSelector the combo box for selecting the model
 * @param platformSelector the combo box for selecting the platform
 */
fun stylizeMainComponents(
    platformSelector: ComboBox<String>,
    modelSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
    llmPlatforms: List<LLMPlatform>,
) {
    val settingsState = SettingsApplicationService.getInstance().state!!

    // Check if the Grazie platform access is available in the current build
    if (isGrazieClassLoaded()) {
        platformSelector.model = DefaultComboBoxModel(llmPlatforms.map { it.name }.toTypedArray())
        platformSelector.selectedItem = settingsState.currentLLMPlatformName
    } else {
        platformSelector.isEnabled = false
    }

    llmUserTokenField.toolTipText = TestSparkToolTipsBundle.defaultValue("llmToken")
    updateLlmUserTokenField(platformSelector, llmUserTokenField, llmPlatforms)

    modelSelector.toolTipText = TestSparkToolTipsBundle.defaultValue("model")
    updateModelSelector(platformSelector, modelSelector, llmUserTokenField, llmPlatforms)
}

/**
 * Retrieves the list of LLMPlatforms.
 *
 * @return The list of LLMPlatforms.
 */
fun getLLLMPlatforms(): List<LLMPlatform> {
    return listOf(OpenAIPlatform(), GraziePlatform())
}

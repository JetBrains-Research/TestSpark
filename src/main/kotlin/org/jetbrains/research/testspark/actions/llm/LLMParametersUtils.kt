package org.jetbrains.research.testspark.actions.llm

import com.google.gson.JsonParser
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.util.io.HttpRequests
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import java.net.HttpURLConnection
import javax.swing.DefaultComboBoxModel
import javax.swing.JTextField

/**
 * Checks if the Grazie class is loaded.
 * @return true if the Grazie class is loaded, false otherwise.
 */
fun isGrazieClassLoaded(): Boolean {
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
fun updateModelSelector(
    platformSelector: ComboBox<String>,
    modelSelector: ComboBox<String>,
    llmUserTokenField: JTextField,
    settingsState: SettingsApplicationState,
    defaultModulesArray: Array<String>,
    lastChosenModule: String,
) {
    if (platformSelector.selectedItem!!.toString() == "Grazie") {
        modelSelector.model = DefaultComboBoxModel(arrayOf("GPT-4"))
        modelSelector.isEnabled = false
        return
    }
    ApplicationManager.getApplication().executeOnPooledThread {
        val modules = getOpenAIModules(llmUserTokenField.text, lastChosenModule)
        modelSelector.removeAllItems()
        if (modules != null) {
            modelSelector.model = DefaultComboBoxModel(modules)
            if (modules.contains(settingsState.model)) modelSelector.selectedItem = settingsState.model
            modelSelector.isEnabled = true
        } else {
            modelSelector.model = DefaultComboBoxModel(defaultModulesArray)
            modelSelector.isEnabled = false
        }
    }
}

/**
 * Retrieves all available models from the OpenAI API using the provided token.
 *
 * @param token Authorization token for the OpenAI API.
 * @return An array of model names if request is successful, otherwise null.
 */
private fun getOpenAIModules(token: String, lastChosenModule: String): Array<String>? {
    val url = "https://api.openai.com/v1/models"

    val httpRequest = HttpRequests.request(url).tuner {
        it.setRequestProperty("Authorization", "Bearer $token")
    }

    val models = mutableListOf<String>()

    try {
        httpRequest.connect {
            if ((it.connection as HttpURLConnection).responseCode == HttpURLConnection.HTTP_OK) {
                val jsonObject = JsonParser.parseString(it.readString()).asJsonObject
                val dataArray = jsonObject.getAsJsonArray("data")
                for (dataObject in dataArray) {
                    val id = dataObject.asJsonObject.getAsJsonPrimitive("id").asString
                    models.add(id)
                }
            }
        }
    } catch (e: HttpRequests.HttpStatusException) {
        return null
    }

    val gptComparator = Comparator<String> { s1, s2 ->
        when {
            s1 == lastChosenModule -> -1
            s2 == lastChosenModule -> 1
            s1.contains("gpt") && s2.contains("gpt") -> s2.compareTo(s1)
            s1.contains("gpt") -> -1
            s2.contains("gpt") -> 1
            else -> s1.compareTo(s2)
        }
    }

    if (models.isNotEmpty()) {
        return models.sortedWith(gptComparator).toTypedArray().filter { !it.contains("vision") }
            .toTypedArray()
    }

    return null
}

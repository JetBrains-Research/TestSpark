package org.jetbrains.research.testspark.display.custom

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.services.LLMSettingsService
import org.jetbrains.research.testspark.settings.llm.LLMSettingsState
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class JUnitCombobox(
    val e: AnActionEvent,
) : ComboBox<JUnitVersion>(JUnitVersion.entries.toTypedArray()) {
    private val llmSettingsState: LLMSettingsState
        get() = e.project!!.getService(LLMSettingsService::class.java).state

    init {
        val detected = findJUnitDependency()

        if (llmSettingsState.junitVersionPriorityCheckBoxSelected && detected.size == 1) {
            this.selectedItem = detected[0]
        } else {
            for (junitVersion in JUnitVersion.entries) {
                if (junitVersion == llmSettingsState.junitVersion) {
                    this.selectedItem = junitVersion
                }
            }
        }

        renderer =
            object : DefaultListCellRenderer() {
                override fun getListCellRendererComponent(
                    list: JList<*>?,
                    value: Any?,
                    index: Int,
                    isSelected: Boolean,
                    cellHasFocus: Boolean,
                ): Component {
                    var name = value
                    if (value is JUnitVersion) {
                        name = value.showName
                        if (detected.contains(value)) {
                            name += " (Detected)"
                        }
                    }
                    return super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus)
                }
            }
    }

    private fun findJUnitDependency(): List<JUnitVersion> {
        val detected: MutableList<JUnitVersion> = mutableListOf()
        val project = e.project!!
        val virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.firstOrNull() ?: return detected

        val index = ProjectRootManager.getInstance(project).fileIndex
        val module = index.getModuleForFile(virtualFile) ?: return detected

        for (orderEntry in ModuleRootManager.getInstance(module).orderEntries) {
            if (orderEntry is LibraryOrderEntry) {
                val libraryName = orderEntry.library?.name ?: continue
                for (junit in JUnitVersion.entries) {
                    if (libraryName.contains(junit.groupId)) {
                        detected.add(junit)
                    }
                }
            }
        }
        return detected
    }
}

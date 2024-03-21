package org.jetbrains.research.testspark.display

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.roots.LibraryOrderEntry
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.ComboBox
import org.jetbrains.research.testspark.core.data.JUnitVersion
import org.jetbrains.research.testspark.services.SettingsApplicationService
import org.jetbrains.research.testspark.settings.SettingsApplicationState
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class JUnitCombobox(val e: AnActionEvent) : ComboBox<JUnitVersion>(JUnitVersion.values()) {
    val settingsState: SettingsApplicationState
        get() = SettingsApplicationService.getInstance().state!!

    init {
        val detected = findJUnitDependency()

        if (settingsState.junitVersionPriorityCheckBoxSelected && detected.size == 1) {
            this.selectedItem = detected[0]
        } else {
            for (junitVersion in JUnitVersion.values()) {
                if (junitVersion.showName == settingsState.junitVersion) {
                    this.selectedItem = junitVersion
                }
            }
        }

        renderer = object : DefaultListCellRenderer() {
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
                for (junit in JUnitVersion.values()) {
                    if (libraryName.contains(junit.groupId)) {
                        detected.add(junit)
                    }
                }
            }
        }
        return detected
    }
}

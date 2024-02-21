package org.jetbrains.research.testspark.display

import com.intellij.openapi.ui.ComboBox
import org.jetbrains.research.testspark.data.JUnitVersion
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class JUnitCombobox : ComboBox<JUnitVersion>(JUnitVersion.values()) {

    var detected: JUnitVersion? = null
        set(value) {
            field = value
            value?.let {
                this.selectedItem = value
            }
        }

    init {
        renderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean,
            ): Component {
                var v = value
                if (value is JUnitVersion) {
                    v = value.showName
                    if (value == detected) {
                        v += " (Detected)"
                    }
                }
                return super.getListCellRendererComponent(list, v, index, isSelected, cellHasFocus)
            }
        }
    }
}

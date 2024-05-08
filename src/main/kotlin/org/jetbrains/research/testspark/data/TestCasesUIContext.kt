package org.jetbrains.research.testspark.data

import java.awt.Color
import javax.swing.JPanel

data class TestCasesUIContext(
    var testCasePanels: HashMap<String, JPanel> = HashMap(),
    var testsSelected: Int = 0,
    var defaultEditorColor: Color? = null,
)

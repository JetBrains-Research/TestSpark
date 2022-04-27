package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.components.DialogPanel
import com.intellij.ui.components.JBLabel
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.ui.GridBag
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Dimension
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.Insets
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JTextField


class TestGeniePanelWrapper : DialogWrapper(true) {

    // max_size, global_timeout, coverage - some TestGenie parameters
    private val maxSizeTextField : JTextField = JTextField()
    private val globalTimeOutTextField : JTextField = JTextField()
    private val coverageTextField : JTextField = JTextField()
    private val toolWindowPanel : JPanel = JPanel(GridBagLayout())
    init {
        init();
        maxSizeTextField.text = "42";
        globalTimeOutTextField.text = "69";
        coverageTextField.text = "true";
    }

    // A helper method to create a createLabel
    private fun createLabel(text: String): JComponent {
        val createLabel = JBLabel(text)
        createLabel.componentStyle = UIUtil.ComponentStyle.SMALL
        createLabel.fontColor = UIUtil.FontColor.BRIGHTER
        createLabel.border = JBUI.Borders.empty(0, 5, 2, 0)
        return createLabel
    }

    fun getContent(): JComponent {
        return toolWindowPanel
    }

    override fun createCenterPanel(): JComponent? {
        // Create a grid bag
        val gb = GridBag()
            .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
            .setDefaultWeightX(1.0)
            .setDefaultFill(GridBagConstraints.HORIZONTAL);

        toolWindowPanel.preferredSize = Dimension(400, 200);

        // Set the elements into the grid. 20% of the horizontal space is the createLabel, 80% is the actual text field (vertically they have equal weights)
        toolWindowPanel.add(createLabel("mode"), gb.nextLine().next().weightx(0.2))
        toolWindowPanel.add(maxSizeTextField, gb.next().weightx(0.8))
        toolWindowPanel.add(createLabel("username"), gb.nextLine().next().weightx(0.2))
        toolWindowPanel.add(globalTimeOutTextField, gb.next().weightx(0.8))
        toolWindowPanel.add(createLabel("password"), gb.nextLine().next().weightx(0.2))
        toolWindowPanel.add(coverageTextField, gb.next().weightx(0.8))

        return toolWindowPanel
    }
}
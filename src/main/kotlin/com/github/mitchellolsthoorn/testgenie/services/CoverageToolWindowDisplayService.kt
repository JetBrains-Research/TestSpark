package com.github.mitchellolsthoorn.testgenie.services

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import org.jdesktop.swingx.JXTitledSeparator
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*

class CoverageToolWindowDisplayService(private val project: Project) {
    var mainPanel: JPanel ?= null
    private var panelTitleAbsolute = JLabel("Absolute Test Coverage")
    private var panelTitleRelative = JLabel("Relative Test Coverage")
    var absoluteLines = JBLabel("Amount of Lines covered: " + " Total: ")
    var absoluteBranch = JBLabel("Amount of Branches covered: " + " Total: ")
    var absoluteMutant = JBLabel("Amount of Mutants covered: "  + " Total: ")
    var relativeLines = JBLabel("Percentage of Lines covered: ")
    var relativeBranch = JBLabel("Percentage of Branches covered: ")
    var relativeMutant = JBLabel("Percentage of Mutants covered: ")

    var listLabels = listOf<JBLabel>(absoluteLines, absoluteBranch, absoluteMutant, relativeLines, relativeBranch, relativeMutant)


    /**
     * Show the labels for statistics on code coverage by tests in "coverage visualisation" tab
     */
    init {
        panelTitleAbsolute.font = Font("Monochrome", Font.BOLD, 20)
        panelTitleRelative.font = Font("Monochrome", Font.BOLD, 20)

        for (listLabel in listLabels) {
            listLabel.font = Font("Monochrome", Font.BOLD, 17)
        }
        mainPanel = FormBuilder.createFormBuilder()
                .setFormLeftIndent(30)
                .addVerticalGap(7)
                .addComponent(panelTitleAbsolute,25)
                .addComponent(absoluteLines, 20)
                .addComponent(absoluteBranch, 20)
                .addComponent(absoluteMutant, 20)
                .addComponent(panelTitleRelative, 25)
                .addComponent(relativeLines, 20)
                .addComponent(relativeBranch, 20)
                .addComponent(relativeMutant, 20)
                .addComponentFillVertically(JPanel(), 20)
                .panel
    }
}
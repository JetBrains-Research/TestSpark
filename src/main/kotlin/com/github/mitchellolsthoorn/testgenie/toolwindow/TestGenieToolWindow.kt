package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import java.awt.Dimension
import java.awt.event.ActionEvent
import javax.swing.JButton
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.util.containers.toArray
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * This class stores the UI of the TestGenie tool window.
 */
class TestGenieToolWindow(_toolWindow: ToolWindow) {

    private val toolWindow = _toolWindow

    private var searchBudget: JSpinner = JSpinner(SpinnerNumberModel(60,0,10000,1))
    private var localSearchBudgetType: ComboBox<LocalSearchBudgetType> = ComboBox(LocalSearchBudgetType.values())
    private var localSearchBudgetValue: JSpinner = JSpinner(SpinnerNumberModel(5,0,10000,1))
    private var stoppingCondition: ComboBox<StoppingCondition> = ComboBox<StoppingCondition>(StoppingCondition.values())
    private var initializationTimeout: JSpinner = JSpinner(SpinnerNumberModel(120,0,10000,1))
    private var minimisationTimeout: JSpinner = JSpinner(SpinnerNumberModel(60,0,10000,1))
    private var assertionTimeout: JSpinner = JSpinner(SpinnerNumberModel(60,0,10000,1))
    private var junitCheckTimeout: JSpinner = JSpinner(SpinnerNumberModel(60,0,10000,1))
    private var population: JSpinner = JSpinner(SpinnerNumberModel(50,0,10000,1))
    private var populationLimit: ComboBox<PopulationLimit> = ComboBox(PopulationLimit.values())
    private var saveButton: JButton? = null

    private var toolWindowPanel: JPanel? = null

    init {
        toolWindowPanel = FormBuilder.createFormBuilder()
                .addLabeledComponent(JBLabel("Search budget"), searchBudget, 1, false)
                .addTooltip("Default 60 seconds")
                .addLabeledComponent(JBLabel("Local search budget type"), localSearchBudgetType, 1, false)
                .addTooltip("Default Time")
                .addLabeledComponent(JBLabel("Local search budget value"), localSearchBudgetValue, 1, false)
                .addTooltip("Default 5")
                .addLabeledComponent(JBLabel("Stopping condition"), stoppingCondition, 1, false)
                .addTooltip("Default: Max statements")
                .addLabeledComponent(JBLabel("Initialization timeout"), initializationTimeout, 1, false)
                .addTooltip("Default 120 seconds")
                .addLabeledComponent(JBLabel("Minimisation timeout"), minimisationTimeout, 1, false)
                .addTooltip("Default 60 seconds")
                .addLabeledComponent(JBLabel("Assertion timeout"), assertionTimeout, 1, false)
                .addTooltip("Default 60 seconds")
                .addLabeledComponent(JBLabel("JUnit check timeout"), junitCheckTimeout, 1, false)
                .addTooltip("Default 60 seconds")
                .addLabeledComponent(JBLabel("Population"), population, 1, false)
                .addTooltip("Default 50")
                .addLabeledComponent(JBLabel("Population limit"), populationLimit, 1, false)
                .addTooltip("Default: Individuals")
                .addComponentFillVertically(JPanel(), 0)
                .panel
        //saveButton?.addActionListener { addListenerForSaveButton(it) }
    }

    /**
     * Returns the panel that is the main wrapper component of the tool window.
     */
    fun getContent(): JComponent? {
        return toolWindowPanel!!
    }

    private fun isModified(): Boolean {
        // TODO: Load state data and compare if it was modified.
        if (searchBudget.value == null) Messages.showErrorDialog("Please specify the value of search budget", "Empty Value Field")
        if (localSearchBudgetValue.value == null) Messages.showErrorDialog("Please specify the value of local search budget", "Empty Value Field")
        if (initializationTimeout.value == null) Messages.showErrorDialog("Please specify the value of initialization timeout", "Empty Value Field")
        if (minimisationTimeout.value == null) Messages.showErrorDialog("Please specify the value of minimization timeout", "Empty Value Field")
        if (assertionTimeout.value == null) Messages.showErrorDialog("Please specify the value of assertion timeout", "Empty Value Field")
        if (junitCheckTimeout.value == null) Messages.showErrorDialog("Please specify the value JUnit check timeout", "Empty Value Field")
        if (population.value == null) Messages.showErrorDialog("Please specify the value of population", "Empty Value Field")
        return true
    }


    /**
     * Adds a listener to the `Save` button to parse, validate and extract the entered values.
     */
//    private val addListenerForSaveButton : (ActionEvent) -> Unit = {
//        if (maxSizeTextField?.text == null || globalTimeOutTextField?.text == null) {
//            Messages.showErrorDialog("Please specify the value", "Empty Value Field")
//        } else {
//            // Validate all the input values
//            val maxSize: Int? = toInt(maxSizeTextField!!.text)
//            val globalTimeout: Int? = toInt(globalTimeOutTextField!!.text)
//            val coverage : Boolean = coverageCombobox?.selectedItem.toString().toBoolean()
//
//            if (maxSize == null || globalTimeout == null) {
//                Messages.showErrorDialog("Please specify a number", "Invalid Input Value")
//            } else {
//                Messages.showInfoMessage(
//                    "Settings saved (not actually, still WIP)" +
//                            "\nMax size: $maxSize" +
//                            "\nGlobal timeout: $globalTimeout" +
//                            "\nCoverage: $coverage",
//                    "Saved"
//                )
//            }
//        }
//    }

    /**
     * Convert a string to an integer, or return null in case of an exception
     */
    private fun toInt(str: String): Int? {
        return try {
            str.toInt()
        } catch (e : java.lang.NumberFormatException) {
            null
        }
    }
}
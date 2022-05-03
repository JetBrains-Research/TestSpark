package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.ui.Messages
import com.intellij.openapi.wm.ToolWindow
import java.awt.event.ActionEvent
import javax.swing.JButton
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.uiDesigner.core.AbstractLayout
import com.intellij.util.containers.toArray
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.GridBag
import org.jdesktop.swingx.JXTitledSeparator
import java.awt.*
import javax.swing.BoxLayout
import javax.swing.GroupLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JSpinner
import javax.swing.JTable
import javax.swing.SpinnerNumberModel

/**
 * This class stores the UI of the TestGenie tool window.
 */
class TestGenieToolWindow(_toolWindow: ToolWindow) {

    private val toolWindow : ToolWindow = _toolWindow

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

    private val title = JLabel("Frequently Used Parameters")
    private var saveButton: JButton = JButton("Save")
    private var resetButton: JButton = JButton("Reset")

    private var toolWindowPanel: JPanel = JPanel()

    init {
        loadState()
        //saveButton.preferredSize = Dimension(40, 20)
        //resetButton.preferredSize = Dimension(40, 20)

//        val gb = GridBag()
//            .setDefaultInsets(Insets(0, 0, AbstractLayout.DEFAULT_VGAP, AbstractLayout.DEFAULT_HGAP))
//            //.setDefaultWeightX(1.0)
//            .setDefaultAnchor(GridBagConstraints.WEST);

        val gbc = GridBagConstraints()
        gbc.anchor = GridBagConstraints.FIRST_LINE_START
        gbc.insets = Insets(10, 0, 10, 0)

        val sus = JPanel(GridBagLayout())
        sus.add(saveButton, gbc)
        gbc.weightx = 1.0
        sus.add(resetButton, gbc)

        sus.preferredSize = Dimension(500, 30)


        toolWindowPanel = FormBuilder.createFormBuilder()
                .setFormLeftIndent(30)
                .addVerticalGap(25)
                .addComponent(title)
                .addLabeledComponent(customLabel("Search budget", "Maximum search duration."), searchBudget, 25, false)
                .addTooltip("Default: 60 seconds")
                .addLabeledComponent(customLabel("Local search budget type", "Interpretation of local search budget value."), localSearchBudgetType, 20, false)
                .addTooltip("Default: Time")
                .addLabeledComponent(customLabel("Local search budget value", "Maximum budget usable for improving individuals per local search."), localSearchBudgetValue, 20, false)
                .addTooltip("Default: 5")
                .addLabeledComponent(customLabel("Stopping condition", "What condition should be checked to end the search."), stoppingCondition, 20, false)
                .addTooltip("Default: Max time")
                .addLabeledComponent(customLabel("Initialization timeout", "Seconds allowed for initializing the search."), initializationTimeout, 20, false)
                .addTooltip("Default: 120 seconds")
                .addLabeledComponent(customLabel("Minimisation timeout", "Seconds allowed for minimization at the end."), minimisationTimeout, 20, false)
                .addTooltip("Default: 60 seconds")
                .addLabeledComponent(customLabel("Assertion timeout", "Seconds allowed for assertion generation at the end."), assertionTimeout, 20, false)
                .addTooltip("Default: 60 seconds")
                .addLabeledComponent(customLabel("JUnit check timeout", "Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability)."), junitCheckTimeout, 20, false)
                .addTooltip("Default: 60 seconds")
                .addLabeledComponent(customLabel("Population", "Population size of genetic algorithm."), population, 20, false)
                .addTooltip("Default: 50")
                .addLabeledComponent(customLabel("Population limit", "What to use as limit for the population size."), populationLimit, 20, false)
                .addTooltip("Default: Individuals")
                .addComponent(sus)
                //.addComponent(resetButton)
                .addComponentFillVertically(JPanel(), 20)
                .panel

                title.font = Font("Monochrome", Font.BOLD, 20)

                resetButton.toolTipText = "Reset all parameters to default."
                saveButton.addActionListener { addListenerForSaveButton(it) }
                resetButton.addActionListener { addListenerForResetButton(it) }
    }

    /**
     * Function that returns a JBLabel that can be customized to its own label text and tooltip text.
     */
    private fun customLabel(label: String, tooltip: String ) : JBLabel {
        var labeled: JBLabel = JBLabel(label)
        labeled.toolTipText = tooltip
        return labeled
    }

    /**
     * Returns the panel that is the main wrapper component of the tool window.
     */
    fun getContent(): JComponent {
        return JBScrollPane(toolWindowPanel)
    }

    private val addListenerForResetButton : (ActionEvent) -> Unit = {
        val state : TestGenieToolWindowState = TestGenieToolWindowService.getInstance().state!!
        state.searchBudget = 60
        state.localSearchBudgetType = LocalSearchBudgetType.TIME
        state.localSearchBudgetValue = 5
        state.stoppingCondition = StoppingCondition.MAXTIME
        state.initializationTimeout = 120
        state.minimisationTimeout = 60
        state.assertionTimeout = 60
        state.junitCheckTimeout = 60
        state.population = 50
        state.populationLimit = PopulationLimit.INDIVIDUALS

        loadState()

        Messages.showInfoMessage("Parameters were restored to defaults", "Restored Successfully")
    }


    /**
     * Adds a listener to the `Save` button to parse, validate and extract the entered values.
     */
    private val addListenerForSaveButton : (ActionEvent) -> Unit = {

        saveState()

        Messages.showInfoMessage("Parameters have been saved successfully", "Saved Successfully")
    }

    private fun loadState() {
        val state : TestGenieToolWindowState = TestGenieToolWindowService.getInstance().state!!

        searchBudget.value = state.searchBudget
        localSearchBudgetType.item = state.localSearchBudgetType
        localSearchBudgetValue.value = state.localSearchBudgetValue
        stoppingCondition.item = state.stoppingCondition
        initializationTimeout.value = state.initializationTimeout
        minimisationTimeout.value = state.minimisationTimeout
        assertionTimeout.value = state.assertionTimeout
        junitCheckTimeout.value = state.junitCheckTimeout
        population.value = state.population
        populationLimit.item = state.populationLimit
    }

    private fun saveState() {
        val state : TestGenieToolWindowState = TestGenieToolWindowService.getInstance().state!!

        state.searchBudget = searchBudget.value as Int
        state.localSearchBudgetType = localSearchBudgetType.item
        state.localSearchBudgetValue = localSearchBudgetValue.value as Int
        state.stoppingCondition = stoppingCondition.item
        state.initializationTimeout = initializationTimeout.value as Int
        state.minimisationTimeout = minimisationTimeout.value as Int
        state.assertionTimeout = assertionTimeout.value as Int
        state.junitCheckTimeout = junitCheckTimeout.value as Int
        state.population = population.value as Int
        state.populationLimit = populationLimit.item
    }

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
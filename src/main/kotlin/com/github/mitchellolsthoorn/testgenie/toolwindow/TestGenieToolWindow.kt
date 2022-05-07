package com.github.mitchellolsthoorn.testgenie.toolwindow

import com.intellij.openapi.ui.Messages

import java.awt.Dimension
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import java.awt.*
import javax.swing.*

/**
 * This class stores the UI of the TestGenie tool window.
 */
class TestGenieToolWindow {

    private var searchBudget: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var localSearchBudgetType: ComboBox<LocalSearchBudgetType> = ComboBox(LocalSearchBudgetType.values())
    private var localSearchBudgetValue: JSpinner = JSpinner(SpinnerNumberModel(5, 0, 10000, 1))
    private var stoppingCondition: ComboBox<StoppingCondition> = ComboBox<StoppingCondition>(StoppingCondition.values())
    private var initializationTimeout: JSpinner = JSpinner(SpinnerNumberModel(120, 0, 10000, 1))
    private var minimisationTimeout: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var assertionTimeout: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var junitCheckTimeout: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var population: JSpinner = JSpinner(SpinnerNumberModel(50, 0, 10000, 1))
    private var populationLimit: ComboBox<PopulationLimit> = ComboBox(PopulationLimit.values())

    private val panelTitle = JLabel("Frequently Used Parameters")
    private var saveButton: JButton = JButton("Save")
    private var resetButton: JButton = JButton("Reset")

    private var toolWindowPanel: JPanel = JPanel()

    private val defaultStr: String = "Default: %s"

    init {
        loadState()

        toolWindowPanel = createToolWindowPanel()

        panelTitle.font = Font("Monochrome", Font.BOLD, 20)
        resetButton.toolTipText = "Reset all parameters to their default values"

        saveButton.addActionListener { addListenerForSaveButton(it) }
        resetButton.addActionListener { addListenerForResetButton(it) }
    }

    /**
     * Creates the entire tool window panel.
     */
    private fun createToolWindowPanel() = FormBuilder.createFormBuilder()
        .setFormLeftIndent(30)
        .addVerticalGap(25)
        .addComponent(panelTitle)
        .addLabeledComponent(customLabel("Search budget", "Maximum search duration."), searchBudget, 25, false)
        .addTooltip(default("60 seconds"))
        .addLabeledComponent(
            customLabel("Local search budget type", "Interpretation of local search budget value."),
            localSearchBudgetType,
            20,
            false
        )
        .addTooltip(default("Time"))
        .addLabeledComponent(
            customLabel(
                "Local search budget value",
                "Maximum budget usable for improving individuals per local search."
            ), localSearchBudgetValue, 20, false
        )
        .addTooltip(default("5"))
        .addLabeledComponent(
            customLabel("Stopping condition", "What condition should be checked to end the search."),
            stoppingCondition,
            20,
            false
        )
        .addTooltip(default("60 seconds"))
        .addLabeledComponent(
            customLabel("Initialization timeout", "Seconds allowed for initializing the search."),
            initializationTimeout,
            20,
            false
        )
        .addTooltip(default("120 seconds"))
        .addLabeledComponent(
            customLabel("Minimisation timeout", "Seconds allowed for minimization at the end."),
            minimisationTimeout,
            20,
            false
        )
        .addTooltip(default("60 seconds"))
        .addLabeledComponent(
            customLabel("Assertion timeout", "Seconds allowed for assertion generation at the end."),
            assertionTimeout,
            20,
            false
        )
        .addTooltip(default("60 seconds"))
        .addLabeledComponent(
            customLabel(
                "JUnit check timeout",
                "Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability)."
            ), junitCheckTimeout, 20, false
        )
        .addTooltip(default("60 seconds"))
        .addLabeledComponent(customLabel("Population", "Population size of genetic algorithm."), population, 20, false)
        .addTooltip(default("50"))
        .addLabeledComponent(
            customLabel("Population limit", "What to use as limit for the population size."),
            populationLimit,
            20,
            false
        )
        .addTooltip(default("Individuals"))
        .addComponent(createSaveAndResetButtons(), 20)
        .addComponentFillVertically(JPanel(), 20)
        .panel

    /**
     * Creates `Save` and `Reset` buttons and aligns them to the left of the tool window.
     *
     * @return the resulting pane with `Save` and `Reset` buttons next to each other
     */
    private fun createSaveAndResetButtons(): JPanel {
        val buttons = JPanel(GridBagLayout())

        val gbc = GridBagConstraints()
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = Insets(10, 0, 10, 5)
        buttons.add(saveButton, gbc)

        gbc.weightx = 1.0
        gbc.insets = Insets(10, 0, 10, 0)
        buttons.add(resetButton, gbc)

        buttons.preferredSize = Dimension(500, 30)
        return buttons
    }

    /**
     * Creates a label (JBLabel) that can be customised to its own label text and tooltip text.
     *
     * @return the created customised label
     */
    private fun customLabel(label: String, tooltip: String): JBLabel {
        val labeled = JBLabel(label)
        labeled.toolTipText = tooltip
        return labeled
    }

    /**
     * Gets the panel that is the main wrapper component of the tool window.
     * The panel is put into a scroll pane so that all the parameters can fit.
     *
     * @return the created tool window pane wrapped into a scroll pane
     */
    fun getContent(): JComponent {
        return JBScrollPane(toolWindowPanel)
    }

    /**
     * Creates a listener for the `Reset` button when the user clicks `Reset`.
     *  It restores the state to the default values and also updates the UI elements.
     *
     */
    private val addListenerForResetButton: (ActionEvent) -> Unit = {
        val choice: Int = Messages.showYesNoCancelDialog(
            "Are you sure you want to reset all the values to defaults?\nThis action cannot be undone",
            "Are You Sure?",
            Messages.getQuestionIcon()
        )

        if (choice == 0) {
            val state: TestGenieToolWindowState = TestGenieToolWindowService.getInstance().state!!
            state.searchBudget = 60
            state.localSearchBudgetType = LocalSearchBudgetType.TIME
            state.localSearchBudgetValue = 5
            state.stoppingCondition = StoppingCondition.MAXTIME
            state.initializationTimeout = 120
            state.minimizationTimeout = 60
            state.assertionTimeout = 60
            state.junitCheckTimeout = 60
            state.population = 50
            state.populationLimit = PopulationLimit.INDIVIDUALS

            loadState()

            Messages.showInfoMessage("Parameters have been restored to defaults", "Restored Successfully")
        }
    }

    /**
     * Creates a listener for the `Save` button when the user clicks 'Save'.
     *  It parses, validates and extracts the entered values.
     */
    private val addListenerForSaveButton: (ActionEvent) -> Unit = {
        saveState()
        Messages.showInfoMessage("Parameters have been saved successfully", "Saved Successfully")
    }

    /**
     * Loads the persisted state and updates the UI elements with the corresponding values.
     */
    private fun loadState() {
        val state: TestGenieToolWindowState = TestGenieToolWindowService.getInstance().state!!

        searchBudget.value = state.searchBudget
        localSearchBudgetType.item = state.localSearchBudgetType
        localSearchBudgetValue.value = state.localSearchBudgetValue
        stoppingCondition.item = state.stoppingCondition
        initializationTimeout.value = state.initializationTimeout
        minimisationTimeout.value = state.minimizationTimeout
        assertionTimeout.value = state.assertionTimeout
        junitCheckTimeout.value = state.junitCheckTimeout
        population.value = state.population
        populationLimit.item = state.populationLimit
    }

    /**
     * Persist the state by reading off the values from the UI elements.
     */
    private fun saveState() {
        val state: TestGenieToolWindowState = TestGenieToolWindowService.getInstance().state!!

        state.searchBudget = searchBudget.value as Int
        state.localSearchBudgetType = localSearchBudgetType.item
        state.localSearchBudgetValue = localSearchBudgetValue.value as Int
        state.stoppingCondition = stoppingCondition.item
        state.initializationTimeout = initializationTimeout.value as Int
        state.minimizationTimeout = minimisationTimeout.value as Int
        state.assertionTimeout = assertionTimeout.value as Int
        state.junitCheckTimeout = junitCheckTimeout.value as Int
        state.population = population.value as Int
        state.populationLimit = populationLimit.item
    }

    /**
     * Creates a string that specifies what is the default value in the format: "Default: <value>".
     *
     * @param value the default value
     * @return a string with the provided default value in the specified format
     */
    private fun default(value: String): String {
        return String.format(defaultStr, value)
    }
}
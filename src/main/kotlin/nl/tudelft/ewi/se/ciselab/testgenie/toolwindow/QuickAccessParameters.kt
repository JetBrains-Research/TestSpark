package nl.tudelft.ewi.se.ciselab.testgenie.toolwindow

import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jdesktop.swingx.JXTitledSeparator
import java.awt.*
import java.awt.event.ActionEvent
import javax.swing.*

/**
 * This class stores the UI of the TestGenie tool window.
 */
class QuickAccessParameters {

    // UI elements for EvoSuite parameters
    private var stoppingCondition: ComboBox<StoppingCondition> = ComboBox<StoppingCondition>(StoppingCondition.values())
    private var searchBudget: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var initializationTimeout: JSpinner = JSpinner(SpinnerNumberModel(120, 0, 10000, 1))
    private var minimisationTimeout: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var assertionTimeout: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var junitCheckTimeout: JSpinner = JSpinner(SpinnerNumberModel(60, 0, 10000, 1))
    private var populationLimit: ComboBox<PopulationLimit> = ComboBox(PopulationLimit.values())
    private var population: JSpinner = JSpinner(SpinnerNumberModel(50, 0, 10000, 1))

    // Save and Reset buttons
    private var saveButton: JButton = JButton("Save")
    private var resetButton: JButton = JButton("Reset")

    // Tool Window panel
    private val panelTitle = JLabel("Quick Access Parameters")
    private var toolWindowPanel: JPanel = JPanel()

    // The tooltip labels
    private var stoppingConditionToolTip =
        JBLabel("Default: 60 seconds", UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER)
    private var populationLimitToolTip =
        JBLabel("Default: 60 seconds", UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER)

    // Template strings for "default" tooltips
    private val defaultStr: String = "Default: %s"


    init {
        // Load the persisted state
        loadState()

        // Create the main panel and set the font of the title
        toolWindowPanel = createToolWindowPanel()
        panelTitle.font = Font("Monochrome", Font.BOLD, 20)

        // Add tooltips to the UI elements for parameters
        addTooltipsToUiElements()

        // Add an action listener to stopping condition combo box to update the "default" tooltip
        fun updateStoppingConditionTooltip() {
            stoppingConditionToolTip.text = default("60 ${stoppingCondition.item.units()}")
        }
        stoppingConditionToolTip.border = JBUI.Borders.emptyLeft(10)
        stoppingCondition.addActionListener { updateStoppingConditionTooltip() }
        updateStoppingConditionTooltip()

        // Add an action listener to population limit combo box to update the "default" tooltip
        fun updatePopulationLimitToolTip() {
            populationLimitToolTip.text = default("60 ${populationLimit.item.toString().toLowerCase()}")
        }
        populationLimitToolTip.border = JBUI.Borders.emptyLeft(10)
        populationLimit.addActionListener { updatePopulationLimitToolTip() }
        updatePopulationLimitToolTip()

        // Add action listeners to Save and Reset buttons
        saveButton.addActionListener { addListenerForSaveButton(it) }
        resetButton.addActionListener { addListenerForResetButton(it) }
    }

    /**
     * Creates the entire tool window panel.
     */
    private fun createToolWindowPanel() = FormBuilder.createFormBuilder()
        // Add indentations from the left border and between the lines, and add title
        .setFormLeftIndent(30)
        .addVerticalGap(25)
        .addComponent(panelTitle)

        // Add `Search Budget` category
        .addComponent(JXTitledSeparator("Search budget"), 35)
        .addLabeledComponent(
            customLabel(
                "Search budget type",
                "What condition should be checked to end the search."
            ), stoppingCondition, 25, false
        )
        .addTooltip(default(StoppingCondition.MAXTIME.toString()))
        .addLabeledComponent(customLabel("Search budget", "Maximum search duration."), searchBudget, 25, false)
        .addComponentToRightColumn(stoppingConditionToolTip, 1)

        // Add `Timeouts` category
        .addComponent(JXTitledSeparator("Timeouts"), 35)
        .addLabeledComponent(
            customLabel(
                "Initialization timeout",
                "Seconds allowed for initializing the search."
            ), initializationTimeout, 25, false
        )
        .addTooltip(default("120 seconds"))
        .addLabeledComponent(
            customLabel(
                "Minimisation timeout",
                "Seconds allowed for minimization at the end."
            ), minimisationTimeout, 20, false
        )
        .addTooltip(default("60 seconds"))
        .addLabeledComponent(
            customLabel(
                "Assertion timeout",
                "Seconds allowed for assertion generation at the end."
            ), assertionTimeout, 20, false
        )
        .addTooltip(default("60 seconds"))
        .addLabeledComponent(
            customLabel(
                "JUnit check timeout",
                "Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability)."
            ), junitCheckTimeout, 20, false
        )
        .addTooltip(default("60 seconds"))

        // Add `Genetic Algorithm` section
        .addComponent(JXTitledSeparator("Genetic Algorithm"), 35)
        .addLabeledComponent(
            customLabel(
                "Population limit",
                "What to use as limit for the population size."
            ), populationLimit, 25, false
        )
        .addTooltip(default("Individuals"))
        .addLabeledComponent(
            customLabel(
                "Population",
                "Population size of genetic algorithm."
            ), population, 20, false
        )
        .addComponentToRightColumn(populationLimitToolTip, 1)

        // Add Save and Reset buttons
        .addComponent(createSaveAndResetButtons(), 20)

        // Add the main panel
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
            val state: TestGenieToolWindowState = QuickAccessParametersService.getInstance().state!!
            state.stoppingCondition = StoppingCondition.MAXTIME
            state.searchBudget = 60
            state.initializationTimeout = 120
            state.minimizationTimeout = 60
            state.assertionTimeout = 60
            state.junitCheckTimeout = 60
            state.populationLimit = PopulationLimit.INDIVIDUALS
            state.population = 50

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
        val state: TestGenieToolWindowState = QuickAccessParametersService.getInstance().state!!

        stoppingCondition.item = state.stoppingCondition
        searchBudget.value = state.searchBudget
        initializationTimeout.value = state.initializationTimeout
        minimisationTimeout.value = state.minimizationTimeout
        assertionTimeout.value = state.assertionTimeout
        junitCheckTimeout.value = state.junitCheckTimeout
        populationLimit.item = state.populationLimit
        population.value = state.population
    }

    /**
     * Persist the state by reading off the values from the UI elements.
     */
    private fun saveState() {
        val state: TestGenieToolWindowState = QuickAccessParametersService.getInstance().state!!

        state.stoppingCondition = stoppingCondition.item
        state.searchBudget = searchBudget.value as Int
        state.initializationTimeout = initializationTimeout.value as Int
        state.minimizationTimeout = minimisationTimeout.value as Int
        state.assertionTimeout = assertionTimeout.value as Int
        state.junitCheckTimeout = junitCheckTimeout.value as Int
        state.populationLimit = populationLimit.item
        state.population = population.value as Int
    }

    /**
     * Adds tooltips to the actual UI elements, not the labels for them.
     */
    private fun addTooltipsToUiElements() {
        stoppingCondition.toolTipText = "What condition should be checked to end the search."
        searchBudget.toolTipText = "Maximum search duration."
        initializationTimeout.toolTipText = "Seconds allowed for initializing the search."
        minimisationTimeout.toolTipText = "Seconds allowed for minimization at the end."
        assertionTimeout.toolTipText = "Seconds allowed for assertion generation at the end."
        junitCheckTimeout.toolTipText =
            "Seconds allowed for checking the generated JUnit files <p/>(e.g., compilation and stability)."
        populationLimit.toolTipText = "What to use as limit for the population size."
        population.toolTipText = "Population size of genetic algorithm."

        resetButton.toolTipText = "Reset all parameters to their default values"
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
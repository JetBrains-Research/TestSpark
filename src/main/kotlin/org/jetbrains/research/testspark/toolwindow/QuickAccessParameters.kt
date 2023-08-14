package org.jetbrains.research.testspark.toolwindow

import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.ActionLink
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import org.jdesktop.swingx.JXTitledSeparator
import org.jetbrains.research.testspark.services.QuickAccessParametersService
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import javax.swing.JButton
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.SpinnerNumberModel

/**
 * This class stores the main panel and the UI of the "Parameters" tool window tab.
 */
class QuickAccessParameters(private val project: Project) {
    // Coverage visualisation toggle
    private val showCoverageCheckbox: JCheckBox = JCheckBox(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("showCoverage"))

    // Default state of the quick access parameters
    private val defaultState = QuickAccessParametersState.DefaultState

    // UI elements for EvoSuite parameters
    private val stoppingCondition: ComboBox<StoppingCondition> = ComboBox<StoppingCondition>(StoppingCondition.values())
    private val searchBudget: JSpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 1))
    private val initializationTimeout: JSpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 1))
    private val minimisationTimeout: JSpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 1))
    private val assertionTimeout: JSpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 1))
    private val junitCheckTimeout: JSpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 1))
    private val populationLimit: ComboBox<PopulationLimit> = ComboBox(PopulationLimit.values())
    private val population: JSpinner = JSpinner(SpinnerNumberModel(0, 0, 10000, 1))

    // Save and Reset buttons
    private val saveButton: JButton = JButton(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("saveButton"))
    private val resetButton: JButton = JButton(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("resetButton"))

    // Link to open settings
    private val settingsLink: ActionLink = ActionLink(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("settingsLink")) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "EvoSuite")
    }

    // Tool Window panel
    private val panelTitle = JLabel(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("quickAccess"))
    private var toolWindowPanel: JPanel = JPanel()

    // The tooltip labels
    private val stoppingConditionToolTip =
        JBLabel(org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("default") + "${defaultState.searchBudget} " + org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("seconds"), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER)
    private val populationLimitToolTip =
        JBLabel(org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("default") + "${defaultState.population} " + org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("seconds"), UIUtil.ComponentStyle.SMALL, UIUtil.FontColor.BRIGHTER)

    // Template strings for "default" tooltips
    private val defaultStr: String = org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("defaultStr")

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
            stoppingConditionToolTip.text = default("${defaultState.searchBudget} ${stoppingCondition.item.units()}")
        }
        stoppingConditionToolTip.border = JBUI.Borders.emptyLeft(10)
        stoppingCondition.addActionListener { updateStoppingConditionTooltip() }
        updateStoppingConditionTooltip()

        // Add an action listener to population limit combo box to update the "default" tooltip
        fun updatePopulationLimitToolTip() {
            populationLimitToolTip.text = default("${defaultState.population} ${populationLimit.item.toString().lowercase()}")
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
        // Add coverage visualisation checkbox
        .addComponent(JXTitledSeparator(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("showCoverageDescription")), 35)
        .addComponent(showCoverageCheckbox, 35)
        // Add `Search Budget` category
        .addComponent(JXTitledSeparator(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("searchBudget")), 35)
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("budgetType"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("budgetType"),
            ),
            stoppingCondition,
            25,
            false,
        )
        .addTooltip(default(StoppingCondition.MAXTIME.toString()))
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("searchBudgetParam"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("initTimeout"),
            ),
            searchBudget,
            25,
            false,
        )
        .addComponentToRightColumn(stoppingConditionToolTip, 1)
        // Add `Timeouts` category
        .addComponent(JXTitledSeparator(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("timeouts")), 35)
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("initTimeout"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("initTimeout"),
            ),
            initializationTimeout,
            25,
            false,
        )
        .addTooltip(default("${defaultState.initializationTimeout} " + org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("seconds")))
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("minimTimeout"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("minimTimeout"),
            ),
            minimisationTimeout,
            20,
            false,
        )
        .addTooltip(default("${defaultState.minimizationTimeout} " + org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("seconds")))
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("assertTimeout"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("assertTimeout"),
            ),
            assertionTimeout,
            20,
            false,
        )
        .addTooltip(default("${defaultState.assertionTimeout} " + org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("seconds")))
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("junitTimeout"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("junitTimeout"),
            ),
            junitCheckTimeout,
            20,
            false,
        )
        .addTooltip(default("${defaultState.junitCheckTimeout} " + org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("seconds")))
        // Add `Genetic Algorithm` section
        .addComponent(JXTitledSeparator(org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("geneticAlg")), 35)
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("populationLim"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("populationLim"),
            ),
            populationLimit,
            25,
            false,
        )
        .addTooltip(default(org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("individuals")))
        .addLabeledComponent(
            customLabel(
                org.jetbrains.research.testspark.TestSparkLabelsBundle.defaultValue("population"),
                org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("population"),
            ),
            population,
            20,
            false,
        )
        .addComponentToRightColumn(populationLimitToolTip, 1)
        // Add Save and Reset buttons and a link to open TestSpark settings
        .addComponent(settingsLink, 20)
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
        gbc.insets = JBUI.insets(10, 0, 10, 5)
        buttons.add(saveButton, gbc)

        gbc.weightx = 1.0
        gbc.insets = JBUI.insets(10, 0)
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
        val choice = JOptionPane.showConfirmDialog(
            null,
            org.jetbrains.research.testspark.TestSparkBundle.message("resetMessage"),
            org.jetbrains.research.testspark.TestSparkBundle.message("confirmationTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
        )

        if (choice == JOptionPane.YES_OPTION) {
            val state: QuickAccessParametersState = QuickAccessParametersService.getInstance().state!!
            state.showCoverage = defaultState.showCoverage
            state.stoppingCondition = defaultState.stoppingCondition
            state.searchBudget = defaultState.searchBudget
            state.initializationTimeout = defaultState.initializationTimeout
            state.minimizationTimeout = defaultState.minimizationTimeout
            state.assertionTimeout = defaultState.assertionTimeout
            state.junitCheckTimeout = defaultState.junitCheckTimeout
            state.populationLimit = defaultState.populationLimit
            state.population = defaultState.population

            loadState()

            Messages.showInfoMessage(org.jetbrains.research.testspark.TestSparkBundle.message("parametersResetMessage"), org.jetbrains.research.testspark.TestSparkBundle.message("parametersResetTitle"))
        }
    }

    /**
     * Creates a listener for the `Save` button when the user clicks 'Save'.
     *  It parses, validates and extracts the entered values.
     */
    private val addListenerForSaveButton: (ActionEvent) -> Unit = {
        saveState()
        Messages.showInfoMessage(org.jetbrains.research.testspark.TestSparkBundle.message("parametersSavedMessage"), org.jetbrains.research.testspark.TestSparkBundle.message("parametersSavedTitle"))
    }

    /**
     * Loads the persisted state and updates the UI elements with the corresponding values.
     */
    private fun loadState() {
        val state: QuickAccessParametersState = QuickAccessParametersService.getInstance().state!!

        showCoverageCheckbox.isSelected = state.showCoverage
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
        val state: QuickAccessParametersState = QuickAccessParametersService.getInstance().state!!

        state.showCoverage = showCoverageCheckbox.isSelected
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
        showCoverageCheckbox.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("showCoverage")
        stoppingCondition.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("stoppingCondition")
        searchBudget.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("searchBudget")
        initializationTimeout.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("initTimeoutPopup")
        minimisationTimeout.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("minimTimeoutPopup")
        assertionTimeout.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("assertTimeoutPopup")
        junitCheckTimeout.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("junitTimeoutPopup")
        populationLimit.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("populationLimPopup")
        population.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("populationPopup")

        resetButton.toolTipText = org.jetbrains.research.testspark.TestSparkToolTipsBundle.defaultValue("resetButton")
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

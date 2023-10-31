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
import org.jetbrains.research.testspark.TestSparkBundle
import org.jetbrains.research.testspark.TestSparkLabelsBundle
import org.jetbrains.research.testspark.TestSparkToolTipsBundle
import org.jetbrains.research.testspark.services.QuickAccessParametersService
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.net.URI
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JSpinner
import javax.swing.JTextPane
import javax.swing.SpinnerNumberModel
import javax.swing.event.HyperlinkEvent

/**
 * This class stores the main panel and the UI of the "Parameters" tool window tab.
 */
class QuickAccessParameters(private val project: Project) {

    private val panelTitle = JLabel(TestSparkLabelsBundle.defaultValue("quickAccess"))

    private val testSparkDescription = JTextPane().apply {
        isEditable = false
        contentType = "text/html"
        addHyperlinkListener { evt ->
            if (HyperlinkEvent.EventType.ACTIVATED == evt.eventType) {
                Desktop.getDesktop().browse(evt.url.toURI())
            }
        }
    }

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
    private val saveButton: JButton = JButton(TestSparkLabelsBundle.defaultValue("saveButton"))
    private val resetButton: JButton = JButton(TestSparkLabelsBundle.defaultValue("resetButton"))

    // Link to documentation
    private val documentationLink = ActionLink(
        TestSparkLabelsBundle.defaultValue("documentationLink"),
        ActionListener { _: ActionEvent? ->
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI("https://github.com/JetBrains-Research/TestSpark"))
            }
        },
    )

    // Link to open settings
    private val settingsLink: ActionLink = ActionLink(TestSparkLabelsBundle.defaultValue("settingsLink")) {
        ShowSettingsUtil.getInstance().showSettingsDialog(project, "Plugin")
    }

    // Tool Window panel
    private var toolWindowPanel: JPanel = JPanel()

    // The tooltip labels
    private val stoppingConditionToolTip =
        JBLabel(
            TestSparkToolTipsBundle.defaultValue("default") + "${defaultState.searchBudget} " + TestSparkToolTipsBundle.defaultValue(
                "seconds",
            ),
            UIUtil.ComponentStyle.SMALL,
            UIUtil.FontColor.BRIGHTER,
        )
    private val populationLimitToolTip =
        JBLabel(
            TestSparkToolTipsBundle.defaultValue("default") + "${defaultState.population} " + TestSparkToolTipsBundle.defaultValue(
                "seconds",
            ),
            UIUtil.ComponentStyle.SMALL,
            UIUtil.FontColor.BRIGHTER,
        )

    // Template strings for "default" tooltips
    private val defaultStr: String = TestSparkLabelsBundle.defaultValue("defaultStr")

    init {
        // Load the persisted state
        loadState()

        panelTitle.font = Font("Monochrome", Font.BOLD, 20)

        // Create the main panel and set the font of the title
        toolWindowPanel = createToolWindowPanel()

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
            populationLimitToolTip.text =
                default("${defaultState.population} ${populationLimit.item.toString().lowercase()}")
        }
        populationLimitToolTip.border = JBUI.Borders.emptyLeft(10)
        populationLimit.addActionListener { updatePopulationLimitToolTip() }
        updatePopulationLimitToolTip()

        // Add action listeners to Save and Reset buttons
        saveButton.addActionListener { addListenerForSaveButton(it) }
        resetButton.addActionListener { addListenerForResetButton(it) }

        testSparkDescription.text = getDescriptionText(getContent().preferredSize.width)
    }

    /**
     * Creates the entire tool window panel.
     */
    private fun createToolWindowPanel() = FormBuilder.createFormBuilder()
        // Add indentations from the left border and between the lines, and add title
        .setFormLeftIndent(30)
        .addVerticalGap(25)
        .addComponent(panelTitle)
        .addComponent(testSparkDescription, 10)
        // Add `Search Budget` category
        .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("searchBudget")), 35)
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("budgetType"),
                TestSparkToolTipsBundle.defaultValue("budgetType"),
            ),
            stoppingCondition,
            25,
            false,
        )
        .addTooltip(default(StoppingCondition.MAXTIME.toString()))
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("searchBudgetParam"),
                TestSparkToolTipsBundle.defaultValue("initTimeout"),
            ),
            searchBudget,
            25,
            false,
        )
        .addComponentToRightColumn(stoppingConditionToolTip, 1)
        // Add `Timeouts` category
        .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("timeouts")), 35)
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("initTimeout"),
                TestSparkToolTipsBundle.defaultValue("initTimeout"),
            ),
            initializationTimeout,
            25,
            false,
        )
        .addTooltip(default("${defaultState.initializationTimeout} " + TestSparkToolTipsBundle.defaultValue("seconds")))
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("minimTimeout"),
                TestSparkToolTipsBundle.defaultValue("minimTimeout"),
            ),
            minimisationTimeout,
            20,
            false,
        )
        .addTooltip(default("${defaultState.minimizationTimeout} " + TestSparkToolTipsBundle.defaultValue("seconds")))
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("assertTimeout"),
                TestSparkToolTipsBundle.defaultValue("assertTimeout"),
            ),
            assertionTimeout,
            20,
            false,
        )
        .addTooltip(default("${defaultState.assertionTimeout} " + TestSparkToolTipsBundle.defaultValue("seconds")))
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("junitTimeout"),
                TestSparkToolTipsBundle.defaultValue("junitTimeout"),
            ),
            junitCheckTimeout,
            20,
            false,
        )
        .addTooltip(default("${defaultState.junitCheckTimeout} " + TestSparkToolTipsBundle.defaultValue("seconds")))
        // Add `Genetic Algorithm` section
        .addComponent(JXTitledSeparator(TestSparkLabelsBundle.defaultValue("geneticAlg")), 35)
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("populationLim"),
                TestSparkToolTipsBundle.defaultValue("populationLim"),
            ),
            populationLimit,
            25,
            false,
        )
        .addTooltip(default(TestSparkToolTipsBundle.defaultValue("individuals")))
        .addLabeledComponent(
            customLabel(
                TestSparkLabelsBundle.defaultValue("population"),
                TestSparkToolTipsBundle.defaultValue("population"),
            ),
            population,
            20,
            false,
        )
        .addComponentToRightColumn(populationLimitToolTip, 1)
        // Add Save and Reset buttons and a link to open TestSpark settings
        .addComponent(documentationLink, 20)
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
            TestSparkBundle.message("resetMessage"),
            TestSparkBundle.message("confirmationTitle"),
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
        )

        if (choice == JOptionPane.YES_OPTION) {
            val state: QuickAccessParametersState = QuickAccessParametersService.getInstance().state!!
            state.stoppingCondition = defaultState.stoppingCondition
            state.searchBudget = defaultState.searchBudget
            state.initializationTimeout = defaultState.initializationTimeout
            state.minimizationTimeout = defaultState.minimizationTimeout
            state.assertionTimeout = defaultState.assertionTimeout
            state.junitCheckTimeout = defaultState.junitCheckTimeout
            state.populationLimit = defaultState.populationLimit
            state.population = defaultState.population

            loadState()

            Messages.showInfoMessage(
                TestSparkBundle.message("parametersResetMessage"),
                TestSparkBundle.message("parametersResetTitle"),
            )
        }
    }

    /**
     * Creates a listener for the `Save` button when the user clicks 'Save'.
     *  It parses, validates and extracts the entered values.
     */
    private val addListenerForSaveButton: (ActionEvent) -> Unit = {
        saveState()
        Messages.showInfoMessage(
            TestSparkBundle.message("parametersSavedMessage"),
            TestSparkBundle.message("parametersSavedTitle"),
        )
    }

    /**
     * Loads the persisted state and updates the UI elements with the corresponding values.
     */
    private fun loadState() {
        val state: QuickAccessParametersState = QuickAccessParametersService.getInstance().state!!

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
        stoppingCondition.toolTipText = TestSparkToolTipsBundle.defaultValue("stoppingCondition")
        searchBudget.toolTipText = TestSparkToolTipsBundle.defaultValue("searchBudget")
        initializationTimeout.toolTipText = TestSparkToolTipsBundle.defaultValue("initTimeoutPopup")
        minimisationTimeout.toolTipText = TestSparkToolTipsBundle.defaultValue("minimTimeoutPopup")
        assertionTimeout.toolTipText = TestSparkToolTipsBundle.defaultValue("assertTimeoutPopup")
        junitCheckTimeout.toolTipText = TestSparkToolTipsBundle.defaultValue("junitTimeoutPopup")
        populationLimit.toolTipText = TestSparkToolTipsBundle.defaultValue("populationLimPopup")
        population.toolTipText = TestSparkToolTipsBundle.defaultValue("populationPopup")

        resetButton.toolTipText = TestSparkToolTipsBundle.defaultValue("resetButton")
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

    private fun getDescriptionText(width: Int): String {
        return "<html><body style='width: $width px;'><font face=\"Monochrome\">" +
            "TestSpark is a plugin for generating unit tests.<br>" +
            "<li>Uses <a href=\"https://www.evosuite.org\">EvoSuite</a> and <a href=\"https://openai.com\">OpenAI</a> models for unit tests generation.</li>" +
            "<li>Generates tests for different test criteria: line coverage, branch coverage, I/O diversity, exception coverage, mutation score.</li>" +
            "<li>Generates unit tests for capturing failures.</li>" +
            "<li>Generate tests for Java classes, method, and single lines.</li>" +
            "</ul><br>" +
            "Initially implemented by <a href=\"https://www.ciselab.nl\">CISELab</a> at <a href=\"https://se.ewi.tudelft.nl\">SERG @ TU Delft</a>, " +
            "TestSpark is currently developed and maintained by <a href=\"https://lp.jetbrains.com/research/ictl/\">ICTL at JetBrains Research</a>.<br>" +
            "<br>" +
            "<strong>DISCLAIMER</strong><br><br>" +
            "TestSpark is currently designed to serve as an experimental tool.<br>" +
            "Please keep in mind that tests generated by TestSpark are meant to augment your existing test suites. " +
            "They are not meant to replace writing tests manually.</font></body></html>"
    }
}

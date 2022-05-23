package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieSettingsService
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState
import nl.tudelft.ewi.se.ciselab.testgenie.services.QuickAccessParametersService

/**
 * This class is used for constructing the necessary parameters for running evosuite
 * as an external process
 *
 * @param projectClassPath Classpath of the project we're generating tests for
 * @param projectPath Working directory for evosuite
 * @param serializeResultPath Location where the search results will be serialized
 * @param classFQN FQN of the Class-under-test
 *
 */
class SettingsArguments(
    private val projectClassPath: String,
    private val projectPath: String,
    private val serializeResultPath: String,
    private val classFQN: String
) {
    private var command: MutableList<String> = mutableListOf(
        "-generateSuite",
        "-serializeResult",
        "-serializeResultPath", serializeResultPath,
        "-base_dir", projectPath,
        "-projectCP", projectClassPath,
        "-Dnew_statistics=false",
        "-class", classFQN,
        "-Dtest_naming_strategy=COVERAGE"
    )

    /**
     * Appends a method parameter to the command.
     * This makes EvoSuite create goals only for a certain method
     * of the CUT.
     *
     * See https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3
     *
     * @param method The descriptor of the method we're generating tests for
     */
    fun forMethod(method: String): SettingsArguments {
        command.addAll(
            listOf(
                "-Dtarget_method=$method"
            )
        )
        return this
    }

    /**
     * Finalizes the parameter construction by applying the user runtime settings
     */
    fun build(): MutableList<String> {
        val toolWindowState = QuickAccessParametersService.getInstance().state
        val settingsState = TestGenieSettingsService.getInstance().state

        if (toolWindowState != null) {
            val params = toolWindowState.serializeChangesFromDefault()
            command.addAll(params)
        }

        if (settingsState != null) {
            val params = settingsState.serializeChangesFromDefault()
            command.addAll(params)
            command.add(createCriterionString(settingsState))
        }
        return command
    }

    companion object {
        /**
         * Creates a string for the criterion parameter in the format required by EvoSuite.
         *
         * @param state the (settings) state that contains all the criteria
         * @return the generated criteria string, in the required format
         */
        private fun createCriterionString(state: TestGenieSettingsState): String {
            val sb = StringBuilder("-Dcriterion=") // e.g "-Dcriterion=BRANCH:WEAKMUTATION",

            if (state.criterionLine) {
                sb.append("LINE:")
            }
            if (state.criterionBranch) {
                sb.append("BRANCH:")
            }
            if (state.criterionException) {
                sb.append("EXCEPTION:")
            }
            if (state.criterionWeakMutation) {
                sb.append("WEAKMUTATION:")
            }
            if (state.criterionOutput) {
                sb.append("OUTPUT:")
            }
            if (state.criterionMethod) {
                sb.append("METHOD:")
            }
            if (state.criterionMethodNoException) {
                sb.append("METHODNOEXCEPTION:")
            }
            if (state.criterionCBranch) {
                sb.append("CBRANCH:")
            }
            if (sb.endsWith(':', true)) {
                sb.deleteCharAt(sb.length - 1)
            }

            val command: String = sb.toString()
            return if (command == "-Dcriterion=") "-Dcriterion=LINE" else command
        }
    }
}

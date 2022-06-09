package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

import nl.tudelft.ewi.se.ciselab.testgenie.services.QuickAccessParametersService
import nl.tudelft.ewi.se.ciselab.testgenie.services.TestGenieSettingsService
import nl.tudelft.ewi.se.ciselab.testgenie.settings.ContentDigestAlgorithm
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState

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
    private val classFQN: String,
    algorithmsToGenerateMap: HashMap<ContentDigestAlgorithm, String> = hashMapOf(
        ContentDigestAlgorithm.DYNAMOSA to "-generateMOSuite",
        ContentDigestAlgorithm.MOSA to "-generateMOSuite",
        ContentDigestAlgorithm.LIPS to "-generateMOSuite",
        ContentDigestAlgorithm.MIO to "-generateMOSuite",
        ContentDigestAlgorithm.RANDOM_SEARCH to "-generateTests",
        ContentDigestAlgorithm.MONOTONIC_GA to "-generateSuite",
        ContentDigestAlgorithm.STANDARD_GA to "-generateSuite",
        ContentDigestAlgorithm.BREEDER_GA to "-generateSuite",
        ContentDigestAlgorithm.STANDARD_GA to "-generateSuite",
        ContentDigestAlgorithm.STEADY_STATE_GA to "-generateSuite",
        ContentDigestAlgorithm.CELLULAR_GA to "-generateSuite",
        ContentDigestAlgorithm.STANDARD_CHEMICAL_REACTION to "-generateSuite",
        ContentDigestAlgorithm.MAP_ELITES to "-generateSuite",
        ContentDigestAlgorithm.ONE_PLUS_LAMBDA_LAMBDA_GA to "-generateSuite",
        ContentDigestAlgorithm.ONE_PLUS_ONE_EA to "-generateSuite",
        ContentDigestAlgorithm.MU_PLUS_LAMBDA_EA to "-generateSuite",
        ContentDigestAlgorithm.MU_LAMBDA_EA to "-generateSuite",
        ContentDigestAlgorithm.NSGAII to "-generateSuite",
        ContentDigestAlgorithm.SPEA2 to "-generateSuite"
    )
) {
    private var command: MutableList<String> = mutableListOf(
        algorithmsToGenerateMap[TestGenieSettingsService.getInstance().state!!.algorithm]!!,
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
     * This makes EvoSuite create goals only for a certain method of the CUT.
     *
     * See https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3
     *
     * @param method The descriptor of the method we're generating tests for
     * @return the instance object
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
     * Appends a line parameter to the command.
     * This makes EvoSuite create goals only for a certain line of the CUT.
     *
     * @param line the selected line
     * @return the instance object
     */
    fun forLine(line: Int): SettingsArguments {
        command.addAll(
            listOf(
                "-Dtarget_line=$line"
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

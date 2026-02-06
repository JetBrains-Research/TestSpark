package org.jetbrains.research.testspark.tools.evosuite

import org.jetbrains.research.testspark.data.evosuite.ContentDigestAlgorithm
import org.jetbrains.research.testspark.settings.evosuite.EvoSuiteSettingsState
import org.jetbrains.research.testspark.settings.evosuite.EvoSuiteSettingsState.DefaultEvoSuiteSettingsState

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
class EvoSuiteSettingsArguments(
    private val projectClassPath: String,
    private val projectPath: String,
    private val serializeResultPath: String,
    private val classFQN: String,
    baseDir: String,
    private val evoSuiteSettingsState: EvoSuiteSettingsState,
) {
    private var command: MutableList<String> =
        mutableListOf(
            algorithmsToGenerateMap[evoSuiteSettingsState.algorithm]!!,
            "-serializeResult",
            "-serializeResultPath",
            serializeResultPath,
            "-base_dir",
            """"$baseDir"""",
            "-projectCP",
            projectClassPath, // will be updated after building
            "-Dnew_statistics=false",
            "-class",
            classFQN,
            "-Dcatch_undeclared_exceptions=false",
            "-Dtest_naming_strategy=COVERAGE",
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
    fun forMethod(method: String): EvoSuiteSettingsArguments {
        command.addAll(
            listOf(
                "-Dtarget_method=$method",
            ),
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
    fun forLine(line: Int): EvoSuiteSettingsArguments {
        command.addAll(
            listOf(
                "-Dtarget_line=$line",
            ),
        )
        return this
    }

    /**
     * Finalizes the parameter construction by applying the user runtime settings
     */
    fun build(isLineCoverage: Boolean = false): MutableList<String> {
        val params = serializeChangesFromDefault()
        command.addAll(params)
        command.add(createCriterionString(evoSuiteSettingsState, isLineCoverage))
        return command
    }

    private fun serializeChangesFromDefault(): List<String> {
        val params = mutableListOf<String>()
        // Parameters from settings menu
        if (evoSuiteSettingsState.sandbox != DefaultEvoSuiteSettingsState.sandbox) {
            params.add("-Dsandbox=${evoSuiteSettingsState.sandbox}")
        }
        if (evoSuiteSettingsState.assertions != DefaultEvoSuiteSettingsState.assertions) {
            params.add("-Dassertions=${evoSuiteSettingsState.assertions}")
        }
        params.add("-Dalgorithm=${evoSuiteSettingsState.algorithm}")
        if (evoSuiteSettingsState.junitCheck != DefaultEvoSuiteSettingsState.junitCheck) {
            params.add("-Djunit_check=${evoSuiteSettingsState.junitCheck}")
        }
        if (evoSuiteSettingsState.minimize != DefaultEvoSuiteSettingsState.minimize) {
            params.add("-Dminimize=${evoSuiteSettingsState.minimize}")
        }
        return params
    }

    companion object {
        private const val GENERATE_SUITE = "-generateSuite"
        private const val GENERATE_MO_SUITE = "-generateMOSuite"
        private const val GENERATE_TESTS = "-generateTests"
        private const val GENERATE_RANDOM = "-generateRandom"

        /**
         * HashMap that maps algorithms to their corresponding generation strings.
         */
        private val algorithmsToGenerateMap: HashMap<ContentDigestAlgorithm, String> =
            hashMapOf(
                ContentDigestAlgorithm.DYNAMOSA to GENERATE_MO_SUITE,
                ContentDigestAlgorithm.MOSA to GENERATE_MO_SUITE,
                ContentDigestAlgorithm.LIPS to GENERATE_RANDOM,
                ContentDigestAlgorithm.MIO to GENERATE_MO_SUITE,
                ContentDigestAlgorithm.RANDOM_SEARCH to GENERATE_TESTS,
                ContentDigestAlgorithm.MONOTONIC_GA to GENERATE_SUITE,
                ContentDigestAlgorithm.STANDARD_GA to GENERATE_SUITE,
                ContentDigestAlgorithm.BREEDER_GA to GENERATE_SUITE,
                ContentDigestAlgorithm.STANDARD_GA to GENERATE_SUITE,
                ContentDigestAlgorithm.STEADY_STATE_GA to GENERATE_SUITE,
                ContentDigestAlgorithm.CELLULAR_GA to GENERATE_SUITE,
                ContentDigestAlgorithm.STANDARD_CHEMICAL_REACTION to GENERATE_SUITE,
                ContentDigestAlgorithm.MAP_ELITES to GENERATE_RANDOM,
                ContentDigestAlgorithm.ONE_PLUS_LAMBDA_LAMBDA_GA to GENERATE_SUITE,
                ContentDigestAlgorithm.ONE_PLUS_ONE_EA to GENERATE_SUITE,
                ContentDigestAlgorithm.MU_PLUS_LAMBDA_EA to GENERATE_SUITE,
                ContentDigestAlgorithm.MU_LAMBDA_EA to GENERATE_SUITE,
                ContentDigestAlgorithm.NSGAII to GENERATE_SUITE,
                ContentDigestAlgorithm.SPEA2 to GENERATE_SUITE,
            )

        /**
         * Creates a string for the criterion parameter in the format required by EvoSuite.
         *
         * @param state the (settings) state that contains all the criteria
         * @return the generated criteria string, in the required format
         */
        private fun createCriterionString(
            evoSuiteSettingsState: EvoSuiteSettingsState,
            isLineCoverage: Boolean,
        ): String {
            val sb = StringBuilder("-Dcriterion=") // e.g "-Dcriterion=BRANCH:WEAKMUTATION",

            if (isLineCoverage) {
                sb.append("LINE:")
                sb.append("BRANCH:")
                return sb.toString()
            }

            if (evoSuiteSettingsState.criterionLine) {
                sb.append("LINE:")
            }
            if (evoSuiteSettingsState.criterionBranch) {
                sb.append("BRANCH:")
            }
            if (evoSuiteSettingsState.criterionException) {
                sb.append("EXCEPTION:")
            }
            if (evoSuiteSettingsState.criterionWeakMutation) {
                sb.append("WEAKMUTATION:")
            }
            if (evoSuiteSettingsState.criterionOutput) {
                sb.append("OUTPUT:")
            }
            if (evoSuiteSettingsState.criterionMethod) {
                sb.append("METHOD:")
            }
            if (evoSuiteSettingsState.criterionMethodNoException) {
                sb.append("METHODNOEXCEPTION:")
            }
            if (evoSuiteSettingsState.criterionCBranch) {
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

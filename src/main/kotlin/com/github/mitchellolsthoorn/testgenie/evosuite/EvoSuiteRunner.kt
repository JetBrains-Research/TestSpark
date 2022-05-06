package com.github.mitchellolsthoorn.testgenie.evosuite

import com.github.mitchellolsthoorn.testgenie.settings.TestGenieSettingsService
import com.github.mitchellolsthoorn.testgenie.settings.TestGenieSettingsState
import com.github.mitchellolsthoorn.testgenie.toolwindow.TestGenieToolWindowService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.io.FileUtilRt
import java.io.File
import java.nio.charset.Charset


class EvoSuiteRunner {

    companion object {
        private val log = Logger.getInstance(Companion::class.java)

        fun runEvoSuite(projectPath: String, projectClassPath: String, classFQN: String): String {

            val javaPath = "java"// TODO: Source from config

            val pluginsPath = System.getProperty("idea.plugins.path")
            val evoSuitePath = "$pluginsPath/TestGenie/lib/evosuite.jar"

            val evoSuiteProcessTimeout: Long = 12000000 // TODO: Source from config

            val ts = System.currentTimeMillis()
            val sep = File.separatorChar
            val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults${sep}"

            val testResultName = "test_gen_result_$ts"
            val serializeResultPath = "\"$testResultDirectory$testResultName\""

            val toolWindowState = TestGenieToolWindowService.getInstance().state
            val settingsState = TestGenieSettingsService.getInstance().state

            val command = arrayOf(
                "-generateSuite",
                "-serializeResult",
                "-serializeResultPath", serializeResultPath,
                "-base_dir", projectPath, // Working directory for evosuite
                "-class", classFQN, // class FQN inside the project classpath of the class we're generating tests for
                "-projectCP", projectClassPath, // class path of the project we're generating tests for
                "-Djunit_tests=true", // disable writing to 'evosuite-tests' in working directory
                "-Dnew_statistics=false", //disable writing to 'evosuite-report' in working directory

                // Parameters from tool window
                "-Dsearch_budget=${toolWindowState!!.searchBudget}",
                "-Dlocal_search_budget=${toolWindowState.localSearchBudgetValue}",
                "-Dlocal_search_budget_type=${toolWindowState.localSearchBudgetType.name}",
                "-Dstopping_condition=${toolWindowState.stoppingCondition.name}",
                "-Dinitialization_timeout=${toolWindowState.initializationTimeout}",
                "-Dminimization_timeout=${toolWindowState.minimisationTimeout}",
                "-Dassertion_timeout=${toolWindowState.assertionTimeout}",
                "-Djunit_check_timeout=${toolWindowState.junitCheckTimeout}",
                "-Dpopulation=${toolWindowState.population}",
                "-Dpopulation_limit=${toolWindowState.populationLimit.name}",

                // Parameters from settings menu
                "-Dsandbox=${settingsState!!.sandbox}",
                "-Dassertions=${settingsState.assertions}",
                "-seed=${settingsState.seed}",
                "-Dalgorithm=${settingsState.algorithm}",
                "-Dconfiguration_id=${settingsState.configurationId}",
                "-Dclient_on_thread=${settingsState.clientOnThread}",
                "-Djunit_check=${settingsState.junitCheck}",
                createCriterionString(settingsState),
                "-Dminimize=${settingsState.minimize}",
                "-Doutput_variables=TARGET_CLASS,criterion,configuration_id,algorithm,Total_Goals,Covered_Goals,Random_Seed,Generations,Total_Time,Size,Result_Size,Length,Result_Length,Total_Branches_Real,Coverage,BranchCoverage,LineCoverage,WeakMutationScore"
            )

            Thread {
                val cmd = ArrayList<String>()
                cmd.add(javaPath)
                cmd.add("-jar")
                cmd.add(evoSuitePath)
                cmd.addAll(command)

                val cmdString = cmd.fold(String()) { acc, e -> acc.plus(e).plus(" ") }

                log.info("Starting EvoSuite with arguments: $cmdString")
                log.info("Results will be saved to $serializeResultPath")

                val evoSuiteProcess = GeneralCommandLine(cmd)
                evoSuiteProcess.charset = Charset.forName("UTF-8")
                evoSuiteProcess.setWorkDirectory(projectPath)

                val handler: ProcessHandler = OSProcessHandler(evoSuiteProcess)

                val outputBuilder = StringBuilder()

                // attach process listener for output
                handler.addProcessListener(object : ProcessAdapter() {
                    override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                        val text = event.text
                        outputBuilder.append(text)
                        log.info(text)
                    }
                })

                handler.startNotify()

                if (!handler.waitFor(evoSuiteProcessTimeout)) {
                    log.error("EvoSuite process exceeded timeout - ${evoSuiteProcessTimeout}ms")
                }

                // TODO: handle stderr separately
//                if (stderr.isNotEmpty()) {
//                    log.error("EvoSuite process exited with error - $stderr")
//                }

            }.start()

            return testResultName
        }

        /**
         * Creates a string for the criterion parameter in the format required by EvoSuite.
         *
         * @param state the (settings) state that contains all the criteria
         * @return the generated criteria string, in the required format
         */
        private fun createCriterionString(state: TestGenieSettingsState): String {
            val sb = StringBuilder("-Dcriterion=")      // e.g "-Dcriterion=BRANCH:WEAKMUTATION",

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

            return sb.toString()
        }
    }
}
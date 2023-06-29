package org.jetbrains.research.testgenie.tools.evosuite

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import org.evosuite.result.TestGenerationResultImpl
import org.evosuite.utils.CompactReport
import org.jetbrains.research.testgenie.data.Report
import org.jetbrains.research.testgenie.data.TestCase
import org.jetbrains.research.testgenie.services.TestCaseDisplayService
import java.io.File
import java.io.FileReader

/**
 * Class used in conjunction with Runner to listen for the results of
 * the generation process. The listener logic runs on a separate thread.
 * Whenever the results are found, they're published on TEST_GENERATION_RESULT_TOPIC
 * and the thread exits.
 *
 * @param project Project context variable which is required for message bus passing
 * @param resultName result path on which to watch for results
 * @param fileUrl the file url (for caching)
 */
class ResultWatcher(
    private val project: Project,
    private val resultName: String,
    private val fileUrl: String,
    private val classFQN: String,
) :
    Runnable {
    private val log = Logger.getInstance(ResultWatcher::class.java)

    override fun run() {
        val sleepDurationMillis: Long = 2000
        val maxWatchDurationMillis: Long = 10000

        val sep = File.separatorChar
        val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"

        val tmpDir = File(testResultDirectory)

        log.info("Started result listener thread for $resultName")

        val startTime = System.currentTimeMillis()

        while (true) {
            val currentTime = System.currentTimeMillis()

            if (currentTime - startTime > maxWatchDurationMillis) {
                log.info("Max watch duration exceeded, exiting Watcher thread for $resultName")
                return
            }

            log.info("Searching for $resultName results in $testResultDirectory")
            val list = tmpDir.list()

            if (list == null) {
                log.info("Empty dir")
            } else {
                for (pathname in list) {
                    if (pathname == resultName) {
                        log.info("Found file $pathname")

                        val gson = Gson()
                        val reader = JsonReader(FileReader("$testResultDirectory$pathname"))

                        val testGenerationResult: CompactReport = gson.fromJson(reader, CompactReport::class.java)
                        project.service<TestCaseDisplayService>().testGenerationResultList.add(
                            Report(
                                testGenerationResult
                            )
                        )
                        project.service<TestCaseDisplayService>().resultName = resultName
                        project.service<TestCaseDisplayService>().fileUrl = fileUrl
                        project.service<TestCaseDisplayService>().packageLine =
                            getPackageFromTestSuiteCode(testGenerationResult.testSuiteCode)
                        project.service<TestCaseDisplayService>().importsCode =
                            getImportsCodeFromTestSuiteCode(testGenerationResult.testSuiteCode)
                        return
                    }
                }
            }
            Thread.sleep(sleepDurationMillis)
        }
    }

    // get junit imports from a generated code
    private fun getImportsCodeFromTestSuiteCode(testSuiteCode: String?): String {
        testSuiteCode ?: return ""
        return testSuiteCode.replace("\r\n", "\n").split("\n").asSequence()
            .filter { it.contains("^import".toRegex()) }
            .filterNot { it.contains("evosuite".toRegex()) }
            .filterNot { it.contains("RunWith".toRegex()) }
            .filterNot { it.contains(classFQN.toRegex()) }
            .joinToString("\n").plus("\n")
    }

    // get package from a generated code
    private fun getPackageFromTestSuiteCode(testSuiteCode: String?): String {
        testSuiteCode ?: return ""
        return testSuiteCode.replace("\r\n", "\n").split("\n")
            .filter { it.contains("^package".toRegex()) }
            .joinToString("\n").plus("\n")
    }
}

package org.jetbrains.research.testspark.tools.evosuite.generation

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.evosuite.utils.CompactReport
import org.jetbrains.research.testspark.data.Report
import org.jetbrains.research.testspark.tools.getImportsCodeFromTestSuiteCode
import org.jetbrains.research.testspark.tools.getPackageFromTestSuiteCode
import org.jetbrains.research.testspark.tools.saveData
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
        log.info("Started result listener thread for $resultName")

        val gson = Gson()
        val reader = JsonReader(FileReader(resultName))

        val testGenerationResult: CompactReport = gson.fromJson(reader, CompactReport::class.java)

        saveData(
            project,
            Report(testGenerationResult),
            getPackageFromTestSuiteCode(testGenerationResult.testSuiteCode),
            getImportsCodeFromTestSuiteCode(testGenerationResult.testSuiteCode, classFQN),
        )
    }
}

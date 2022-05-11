package nl.tudelft.ewi.se.ciselab.testgenie.evosuite

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import org.evosuite.utils.CompactReport
import java.io.File
import java.io.FileReader

/**
 * Class used in conjunction with Runner to listen for the results of
 * the generation process. The listener logic runs on a separate thread.
 * Whenever the results are found, they're published on TEST_GENERATION_RESULT_TOPIC
 * and the thread exits.
 *
 * @param project Project context variable which is required for message bus passing
 * @param resultPath result path on which to watch for results
 */
class ResultWatcher(private val project: Project, private val resultPath: String) : Runnable {
    private val log = Logger.getInstance(ResultWatcher::class.java)

    override fun run() {
        val sleepDurationMillis: Long = 2000
        val maxWatchDurationMillis: Long = 10000

        val sep = File.separatorChar
        val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"

        val tmpDir = File(testResultDirectory)

        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        log.info("Started result listener thread for $resultPath")

        val startTime = System.currentTimeMillis();

        while (true) {
            val currentTime = System.currentTimeMillis();

            if (currentTime - startTime > maxWatchDurationMillis) {
                log.info("Max watch duration exceeded, exiting Watcher thread for $resultPath")
                return
            }

            log.info("Searching for $resultPath results in $testResultDirectory")
            val list = tmpDir.list()

            if (list == null) {
                log.info("Empty dir")
            } else {
                for (pathname in list) {
                    if (pathname == resultPath) {
                        log.info("Found file $pathname")

                        val gson = Gson()
                        val reader = JsonReader(FileReader("$testResultDirectory$pathname"))

                        val testGenerationResult: CompactReport = gson.fromJson(reader, CompactReport::class.java)

                        log.info("Publishing test generation result to ${TEST_GENERATION_RESULT_TOPIC.displayName}")
                        project.messageBus.syncPublisher(TEST_GENERATION_RESULT_TOPIC)
                            .testGenerationResult(testGenerationResult)
                        log.info("Exiting Watcher thread for $resultPath")
                        return
                    }
                }
            }
            Thread.sleep(sleepDurationMillis)
        }
    }
}

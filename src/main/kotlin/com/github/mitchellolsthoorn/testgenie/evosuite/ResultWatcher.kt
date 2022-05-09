package com.github.mitchellolsthoorn.testgenie.evosuite

import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.io.FileUtilRt
import org.evosuite.utils.CompactReport
import java.io.File
import java.io.FileReader

/**
 * Class used in conjuction with Runner
 *
 * @param project
 * @param resultPath
 */
class ResultWatcher(private val project: Project, private val resultPath: String) : Runnable {
    private val log = Logger.getInstance(ResultWatcher::class.java)

    override fun run() {
        val watcherSleepDurationMillis: Long = 5000

        val sep = File.separatorChar
        val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"

        val tmpDir = File(testResultDirectory)

        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        log.info("Started result listener thread for $resultPath")

        while (true) {
            Thread.sleep(watcherSleepDurationMillis)
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
                        log.info("Exiting Watcher thread for $pathname")
                        return
                    }
                }
            }
        }
    }
}

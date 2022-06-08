package nl.tudelft.ewi.se.ciselab.testgenie.cache

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import nl.tudelft.ewi.se.ciselab.testgenie.editor.Workspace

class CacheService(private val project: Project) {
    private val logger: Logger = Logger.getInstance(this.javaClass)

    private val cache: HashMap<String, ArrayList<Workspace.TestJob>> = hashMapOf()

    fun updateTestJobCache() {
    }

    fun addTestJobCache(fileUrl: String, testJob: Workspace.TestJob) {
        logger.info("Adding tests to cache for file $fileUrl")

        val list = cache.getOrDefault(fileUrl, arrayListOf())
        list.add(testJob)
    }

    fun invalidateCacheForFile(fileUrl: String) {
        logger.info("Removing all tests for file $fileUrl")
        cache.remove(fileUrl)
    }

    fun retrieveTestsCoveringLine(fileUrl: String, line: Int): Workspace.TestJob? {
        val cachedTests = cache[fileUrl] ?: return null
        // TODO: decide if test results past the first should be taken into account
        val latestTestResults = cachedTests.lastOrNull() ?: return null
        // assume user has shitted up their workspace -> valdiate tests
        // later perform static validation :)
        if (latestTestResults.testEdits.isEmpty()) {
            // case user hasn't edited tests
            // we still need to validate tests
        } else {
            // case user has edited tests
        }

        return null
    }
}

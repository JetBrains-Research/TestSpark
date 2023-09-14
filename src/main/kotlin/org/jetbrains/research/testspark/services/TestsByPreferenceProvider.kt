package org.jetbrains.research.testspark.services

import java.util.HashMap
import java.util.ArrayList
import org.jetbrains.research.testspark.data.TestState
import org.jetbrains.research.testspark.helpers.storage.KeyValueStoreFactory
import java.io.File
import java.nio.file.Path

/**
 * This class retrieves all liked and disliked sources of tests from provided directories utilizing a list of test names.
 */
class TestsByPreferenceProvider {
    class Test(val name: String, val code: String)

    /**
     * Collects liked/disliked tests from provided list of test names with their source code in Map and returns it
     */
    fun retrieveTests(likedTestsDir: Path, dislikedTestsDir: Path, testNames: List<String>): Map<TestState, List<Test>> {
        val likedTestsStore = KeyValueStoreFactory.create(likedTestsDir)
        val dislikedTestsStore = KeyValueStoreFactory.create(dislikedTestsDir)

        val result = HashMap<TestState, MutableList<Test>>()
        result.put(TestState.LIKED, ArrayList<Test>())
        result.put(TestState.DISLIKED, ArrayList<Test>())

        try {
            testNames.forEach{testName ->
                val key: ByteArray = testName.toByteArray(Charsets.UTF_8)
                if (likedTestsStore.contains(key)) {
                    val code = String(likedTestsStore.loadValue(key)!!, Charsets.UTF_8);
                    result.get(TestState.LIKED)?.add(Test(testName, code))
                }
                else if (dislikedTestsStore.contains(key)) {
                    val code = String(dislikedTestsStore.loadValue(key)!!, Charsets.UTF_8);
                    result.get(TestState.DISLIKED)?.add(Test(testName, code))
                }
            }

            return result
        }
        finally {
            dislikedTestsStore.close()
            likedTestsStore.close()
        }
    }

    /**
     * Read liked/disliked tests according to test names stored in a file separated by newlines
     */
    fun retrieveTests(likedTestsDir: Path, dislikedTestsDir: Path, testNamesFilepath: Path) {
        val testNames: List<String> = File(testNamesFilepath.toString()).readLines()
        return retrieveTests(likedTestsDir, dislikedTestsDir, testNamesFilepath)
    }
}
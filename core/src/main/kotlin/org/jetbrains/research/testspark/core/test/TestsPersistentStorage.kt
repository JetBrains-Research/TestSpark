package org.jetbrains.research.testspark.core.test

/**
 * The TestPersistentStorage interface represents a contract for saving generated tests to a specified file system location.
 */
interface TestsPersistentStorage {

    /**
     * Save the generated tests to a specified directory.
     *
     * @param packageString The package string where the generated tests will be saved.
     * @param code The generated test code.
     * @param resultPath The result path where the generated tests will be saved.
     * @param testFileName The name of the test file.
     * @return The path where the generated tests are saved.
     */
    fun saveGeneratedTest(packageString: String, code: String, resultPath: String, testFileName: String): String
}

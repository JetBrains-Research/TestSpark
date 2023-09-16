import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File

/**
 * This class manages test cases stored in a JSON file. It provides functionalities to add and remove test cases
 * from the JSON file, while keeping an in-memory representation of the JSON object for efficient read and write operations.
 *
 * @property projectBasePath the base path of the project where the JSON file is located.
 * @property fileName the name of the JSON file that stores the test cases.
 */
class ReactedTestManager(private val projectBasePath: String, private val fileName: String) {

    private val gson = Gson()
    private val file: File = File(projectBasePath, fileName)
    private var jsonObject: JsonObject = if (file.exists()) JsonParser.parseString(file.readText()).asJsonObject else JsonObject()

    /**
     * Adds a new test case to the JSON file.
     * @param testName the name of the test case to be added.
     * @param testCode the code of the test case to be added.
     */
    fun saveTestCase(testName: String, testCode: String) {
        if (jsonObject.has(testName)) return
        jsonObject.addProperty(testName, testCode)
        file.writeText(gson.toJson(jsonObject))
    }

    /**
     * Removes a test case from the JSON file identified by its name.
     * @param testName the name of the test case to be removed.
     */
    fun deleteTestCase(testName: String) {
        if (!jsonObject.has(testName)) return
        jsonObject.remove(testName)
        file.writeText(gson.toJson(jsonObject))
    }
}

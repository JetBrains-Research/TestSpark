package org.jetbrains.research.testspark.helpers.storage

import java.io.IOException
import java.nio.file.Path
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.FileReader
import java.io.FileWriter


/**
 * Implements CRUD operations with provided json file.
 * In order to use nested key use syntax 'a.b.c' -> json["a"]["b"]["c"]
 */
class JsonKeyValueStore(private val filepath: Path) : KeyValueStore {
    override operator fun contains(key: String): Boolean {
        val json = loadJsonFromFile()
        return isNestedKeyExists(json, key)
    }


    override fun get(key: String): String? {
        val json = loadJsonFromFile()
        val nestedKeyValue = getNestedKey(json, key)
        return nestedKeyValue
    }

    override fun upsert(key: String, value: String) {
        val json = loadJsonFromFile()
        upsertNestedKey(json, key, value)
        saveJsonToFile(json)
    }


    override fun remove(key: String): Boolean {
        val json = loadJsonFromFile()
        val removed = removeNestedKey(json, key)
        saveJsonToFile(json)
        return removed;
    }

    // Load JSON from a file and return it as a JsonObject
    fun loadJsonFromFile(): JsonObject {
        println("filepath: '${filepath.toString()}'")
        return FileReader(filepath.toString()).use { reader ->
            JsonParser.parseReader(reader).asJsonObject
        }
    }

    // Save a JsonObject to a file
    fun saveJsonToFile(json: JsonObject) {
        println("filepath: '${filepath.toString()}'")
        FileWriter(filepath.toString()).use { writer ->
            json.toString().let {
                writer.write(it)
            }
        }
    }

    // Remove a nested key from the JsonObject
    fun removeNestedKey(jsonObject: JsonObject, key: String): Boolean {
        val keys = key.split(".")
        var currentObject = jsonObject

        for (i in 0 until keys.size - 1) {
            val k = keys[i]
            if (!currentObject.has(k) || !currentObject[k].isJsonObject) {
                return false // Key or intermediate object doesn't exist
            }
            currentObject = currentObject.getAsJsonObject(k)
        }

        val lastKey = keys.last()
        if (currentObject.has(lastKey)) {
            currentObject.remove(lastKey)
            return true // Key was removed
        }

        return false // Key didn't exist
    }

    fun upsertNestedKey(jsonObject: JsonObject, key: String, value: String) {
        val keys = key.split(".")
        var currentObject = jsonObject

        for (i in 0 until keys.size - 1) {
            val k = keys[i]
            if (!currentObject.has(k) || !currentObject[k].isJsonObject) {
                // Create a new JsonObject if the key doesn't exist or is not an object
                currentObject.add(k, JsonObject())
            }
            currentObject = currentObject.getAsJsonObject(k)
        }

        // Set the final key to the provided value
        currentObject.addProperty(keys.last(), value)
    }

    // Recursive function to get a nested key
    fun getNestedKey(jsonObject: JsonObject, key: String): String? {
        val keys = key.split(".")
        var currentObject: JsonObject = jsonObject

        for (i in 0 until keys.size - 1) {
            val k = keys[i]
            if (!(currentObject.has(k) && currentObject[k].isJsonObject)) {
                return null;
            }

            currentObject = currentObject.getAsJsonObject(k)
        }

        val lastKey = keys.last()
        if (currentObject.has(lastKey)) {
            return currentObject.get(lastKey).asString
        }
        else {
            return null;
        }
    }


    fun isNestedKeyExists(jsonObject: JsonObject, key: String): Boolean {
        val value: String? = getNestedKey(jsonObject, key)
        if (value == null) {
            return false
        }
        else {
            return true
        }
    }
}
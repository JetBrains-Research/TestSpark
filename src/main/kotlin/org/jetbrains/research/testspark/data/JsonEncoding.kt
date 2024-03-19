package org.jetbrains.research.testspark.data

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class JsonEncoding {
    companion object {
        fun decode(jsonString: String): MutableList<String> =
            Json.decodeFromString(ListSerializer(String.serializer()), jsonString) as MutableList<String>

        fun encode(values: MutableList<String>): String {
            var jsonString = Json.encodeToString(
                ListSerializer(String.serializer()),
                values,
            )
            val replacements = mapOf(
                "\\n" to "\n",
                "\\t" to "\t",
                "\\r" to "\r",
                "\\\\" to "\\",
                "\\\"" to "\"",
                "\\'" to "\'",
                "\\b" to "\b",
                "\\f" to "\u000c",
            )
            replacements.forEach { (key, value) ->
                jsonString = jsonString.replace(key, value)
            }
            return jsonString
        }
    }
}

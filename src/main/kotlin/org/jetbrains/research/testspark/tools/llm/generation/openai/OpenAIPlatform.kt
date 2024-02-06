package org.jetbrains.research.testspark.tools.llm.generation.openai

import com.google.gson.JsonParser
import com.intellij.util.io.HttpRequests
import org.jetbrains.research.testspark.bundles.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.tools.llm.generation.LLMPlatform
import java.net.HttpURLConnection

class OpenAIPlatform(
    override val name: String = TestSparkDefaultsBundle.defaultValue("openAI"),
    override var token: String = TestSparkDefaultsBundle.defaultValue("openAIToken"),
    override var model: String = TestSparkDefaultsBundle.defaultValue("openAIModel"),
) : LLMPlatform {
    override fun getModels(providedToken: String): Array<String> {
        val url = "https://api.openai.com/v1/models"

        val httpRequest = HttpRequests.request(url).tuner {
            it.setRequestProperty("Authorization", "Bearer $providedToken")
        }

        val models = mutableListOf<String>()

        try {
            httpRequest.connect {
                if ((it.connection as HttpURLConnection).responseCode == HttpURLConnection.HTTP_OK) {
                    val jsonObject = JsonParser.parseString(it.readString()).asJsonObject
                    val dataArray = jsonObject.getAsJsonArray("data")
                    for (dataObject in dataArray) {
                        val id = dataObject.asJsonObject.getAsJsonPrimitive("id").asString
                        models.add(id)
                    }
                }
            }
        } catch (e: HttpRequests.HttpStatusException) {
            return arrayOf("")
        }

        val gptComparator = Comparator<String> { s1, s2 ->
            when {
                s1.contains("gpt") && s2.contains("gpt") -> s2.compareTo(s1)
                s1.contains("gpt") -> -1
                s2.contains("gpt") -> 1
                else -> s1.compareTo(s2)
            }
        }

        if (models.isNotEmpty()) {
            return models.sortedWith(gptComparator).toTypedArray().filter { !it.contains("vision") }
                .toTypedArray()
        }

        return arrayOf("")
    }
}

package org.jetbrains.research.testspark.core.utils

import java.io.File

class PipelineUtils {
    companion object {
        val dataPath: String get() = System.getenv("PROMPT_DATA_PATH") ?: "./"

        fun savePrompt(
            content: String,
            name: String,
        ) {
            val file = File("$dataPath/prompts/$name.txt")
            file.parentFile.mkdirs()
            file.writeText(content)
        }
    }
}

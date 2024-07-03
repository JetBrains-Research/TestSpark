package org.jetbrains.research.testspark.tools.strategies

import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import org.jetbrains.research.testspark.core.utils.Language
import java.io.File

class TestProcessorStrategies {
    companion object {
        fun getRunner(language: Language, homeDirectory : String): File {
            return when (language) {
                Language.Java -> File(homeDirectory).walk()
                    .filter {
                        val isJavaName = if (DataFilesUtil.isWindows()) it.name.equals("java.exe") else it.name.equals("java")
                        isJavaName && it.isFile
                    }
                    .first()

                Language.Kotlin -> File(homeDirectory).walk()
                    .filter {
                        val isKotlinName = if (DataFilesUtil.isWindows()) it.name.equals("kotlin.exe") else it.name.equals("kotlin")
                        isKotlinName && it.isFile
                    }
                    .first()
            }
        }
    }
}
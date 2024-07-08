package org.jetbrains.research.testspark.tools.strategies

import org.jetbrains.research.testspark.core.utils.DataFilesUtil
import java.io.File

class TestProcessorStrategies {
    companion object {
        fun getRunner(homeDirectory: String): File {
            return File(homeDirectory).walk()
                .filter {
                    val isJavaName =
                        if (DataFilesUtil.isWindows()) it.name.equals("java.exe") else it.name.equals("java")
                    isJavaName && it.isFile
                }
                .first()
        }
    }
}

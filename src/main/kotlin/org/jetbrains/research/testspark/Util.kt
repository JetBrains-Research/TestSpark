package org.jetbrains.research.testspark

import com.intellij.openapi.util.io.FileUtilRt
import java.io.File
import java.util.Locale

class Util {
    companion object {
        fun makeTmp() {
            val sep = File.separatorChar
            val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testSparkResults$sep"

            val tmpDir = File(testResultDirectory)

            if (!tmpDir.exists()) {
                tmpDir.mkdirs()
            }
        }

        fun makeDir(path: String) {
            val dir = File(path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }

        val classpathSeparator: Char
            get() {
                var sep = ':'
                if (isWindows()) {
                    sep = ';'
                }
                return sep
            }

        fun isWindows(): Boolean {
            val os = System.getProperty("os.name").lowercase(Locale.getDefault())
            return (os.indexOf("win") >= 0)
        }
    }
}

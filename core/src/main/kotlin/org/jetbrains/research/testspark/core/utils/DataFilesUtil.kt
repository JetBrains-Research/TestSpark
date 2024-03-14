package org.jetbrains.research.testspark.core.utils

import java.io.File
import java.util.Locale

class DataFilesUtil {
    companion object {
        fun makeTmp(tempDir: String) {
            val sep = File.separatorChar
            val testResultDirectory = "${tempDir}${sep}testSparkResults${sep}"

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

        fun cleanFolder(path: String) {
            val folder = File(path)

            if (!folder.exists()) return

            if (folder.isDirectory) {
                val files = folder.listFiles()
                if (files != null) {
                    for (file in files) {
                        if (file.isDirectory) {
                            cleanFolder(file.absolutePath)
                        } else {
                            file.delete()
                        }
                    }
                }
            }
            folder.delete()
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

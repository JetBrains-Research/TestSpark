package org.jetbrains.research.testspark

import com.intellij.openapi.util.io.FileUtilRt
import java.io.File

class DataFilesUtil {
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
    }
}

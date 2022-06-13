package nl.tudelft.ewi.se.ciselab.testgenie

import com.intellij.openapi.util.io.FileUtilRt
import java.io.File

class Util {
    companion object {
        fun makeTmp() {
            val sep = File.separatorChar
            val testResultDirectory = "${FileUtilRt.getTempDirectory()}${sep}testGenieResults$sep"

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
    }
}

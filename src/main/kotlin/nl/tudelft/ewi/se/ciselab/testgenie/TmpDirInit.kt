package nl.tudelft.ewi.se.ciselab.testgenie

import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * Makes sure the folder which is used for outputting and collecting test
 * results exists
 */
class TmpDirInit : StartupActivity {
    override fun runActivity(project: Project) {
        Util.makeTmp()
    }
}

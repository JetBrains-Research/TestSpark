package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor

/**
 * Class to hold the Welcome Idea frame.
 *
 * @param remoteRobot the robot used for actions
 * @param remoteComponent the component associated with the class
 */
@FixtureName("Welcome Frame")
@DefaultXpath("type", "//div[@class='FlatWelcomeFrame']")
class WelcomeFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    // Action to press "Open project" button
    private val openProject
        get() = actionLink(
            byXpath(
                "//div[(@accessiblename='Open or Import' and @class='JButton') or (@class='MainButton' and @text='Open') or (@class='JButton' and @defaultIcon='open.svg')]"
            )
        )

    // Press ok button to open file
    private val ok
        get() = button(byXpath("//div[@text='OK']"))

    /**
     * Verify that the selected project has appeared in the tab.
     *
     * @param projectName the name of the project
     */
    private fun verifyProjectTreeReady(projectName: String) {
        val tree = actionLink(byXpath("//div[@class='Tree']"))
        tree.findText(projectName).click()

        waitFor {
            tree.data.hasText(projectName)
        }
    }

    /**
     * Method to open project based on provided project name
     *
     * @param projectName the name of the project
     */
    fun open(projectName: String) {
        openProject.click()

        val path = System.getProperty("user.dir") + if (remoteRobot.isWin()) {
            "\\src\\test\\resources\\project\\$projectName"
        } else if (remoteRobot.isLinux() || remoteRobot.isMac()) {
            "/src/test/resources/project/$projectName"
        } else {
            assert(false) { "Bad test OS: " + remoteRobot.os }
        }

        textField(byXpath("//div[@class='BorderlessTextField']")).text = path

        verifyProjectTreeReady(projectName)

        waitFor {
            ok.isEnabled()
        }

        ok.click()
    }
}

package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor

/**
 * Function, which looks for the TabLabel.
 */
fun ContainerFixture.tabLabel(name: String): TabLabel {
    val xpath = byXpath("$name", "//div[@accessiblename='$name' and @class='TabLabel']")
    waitFor {
        findAll<TabLabel>(xpath).isNotEmpty()
    }
    return findAll<TabLabel>(xpath).first()
}

/**
 * This class represents the TabLAbel.
 */
@FixtureName("TabLabel")
class TabLabel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ComponentFixture(remoteRobot, remoteComponent)
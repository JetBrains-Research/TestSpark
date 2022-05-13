package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath

@FixtureName("Tool Window Frame")
@DefaultXpath("type", "//div[@class='BorderLayoutPanel'][.//div[@class='BaseLabel']]")
class ToolWindowFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    val parametersTab
        get() = actionLink(byXpath("//div[@text='Parameters']"))

    fun findQuickAccessParameters() {
        parametersTab.click()
    }
}

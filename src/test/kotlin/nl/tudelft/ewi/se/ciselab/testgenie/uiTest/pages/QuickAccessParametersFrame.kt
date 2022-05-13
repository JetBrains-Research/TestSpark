package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.tabLabel

@FixtureName("Quick Access parameters Frame")
@DefaultXpath("type", "//div[@class='ToolWindowHeader']//div[@class='JPanel']")
class QuickAccessParametersFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

    val parametersTab
        get() = tabLabel("//div[@text='Parameters']")

    fun openQuickAccessParameters() {
//        waitFor(Duration.ofSeconds(15)) {
//            parametersTab.= "Parameters"
//            if (searchTextBox.text == "TestGenie") {
//                return@waitFor true
//            }
//            return@waitFor false
//        }
    }
}

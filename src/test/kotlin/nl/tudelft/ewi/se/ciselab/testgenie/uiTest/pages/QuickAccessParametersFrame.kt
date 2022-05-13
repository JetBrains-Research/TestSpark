package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.pages

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures.openContentTabLabel

@FixtureName("Quick Access parameters Frame")
@DefaultXpath("type", "//div[@class='ToolWindowHeader']//div[@class='JPanel']")
class QuickAccessParametersFrame(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) :
    CommonContainerFixture(remoteRobot, remoteComponent) {

//    val parametersTab
//        get() = openContentTabLabel("//div[@text='Parameters']", 10)

    val toolWindowContentPanel
        get() = find<ContainerFixture>(byXpath("//div[@class='TabPanel'][.//div[@class='ContentComboLabel']]"))

    val titleGetFindJLabel
        get() = jLabel(byXpath("//div[@javaclass='javax.swing.JLabel']"))

    fun openQuickAccessParameters() {
        val cotentTabLabel = openContentTabLabel("//div[@text='Parameters']", 10L)
        with(toolWindowContentPanel) {
            if (hasText("Parameters")) {
                findText("Parameters").click()
            }
        }
    }
}

package com.github.mitchellolsthoorn.testgenie.uiTest

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.DefaultXpath
import com.intellij.remoterobot.search.locators.byXpath
import com.github.mitchellolsthoorn.testgenie.uiTest.pages.WelcomeFrame
import com.github.mitchellolsthoorn.testgenie.uiTest.utils.RemoteRobotExtension
import com.github.mitchellolsthoorn.testgenie.uiTest.utils.StepsLogger
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration
// The code here was copied from JetBrains/intellij-ui-test-robot library, in order to experiment with the UI testing.
@ExtendWith(RemoteRobotExtension::class)
class SayHelloKotlinTest {
    init {
        StepsLogger.init()
    }

    @Test
    fun checkHelloMessage(remoteRobot: RemoteRobot) = with(remoteRobot) {
        find(WelcomeFrame::class.java, timeout = Duration.ofSeconds(10)).apply {
            if (hasText("Welcome to IntelliJ IDEA'")) {
                findText("Welcome to IntelliJ IDEA'").click()
            } else {
                moreActions.click()
                heavyWeightPopup.findText("Say Hello").click()
            }
        }

        val helloDialog = find(HelloWorldDialog::class.java)

        assert(helloDialog.textPane.hasText("Hello World!"))
        helloDialog.ok.click()
    }

    @DefaultXpath("title Hello", "//div[@title='Hello' and @class='MyDialog']")
    class HelloWorldDialog(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ContainerFixture(remoteRobot, remoteComponent) {
        val textPane: ComponentFixture
            get() = find(byXpath("//div[@class='Wrapper']//div[@class='JEditorPane']"))
        val ok: ComponentFixture
            get() = find(byXpath("//div[@class='JButton' and @text='OK']"))
    }
}
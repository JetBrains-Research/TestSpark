package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

/**
 * Function, which looks for the TabLabel.
 */
fun ContainerFixture.openContentTabLabel(name: String, seconds: Long): ContentTabLabel {
    val xpath = byXpath("$name", "//div[@accessiblename='$name' and @class='ContentTabLabel']")
    waitFor(Duration.ofSeconds(seconds)) {
        findAll<ContentTabLabel>(xpath).isNotEmpty()
    }
    return findAll<ContentTabLabel>(xpath).first()
}

/**
 * This class represents the TabLAbel.
 */
@FixtureName("ContentTabLabel")
class ContentTabLabel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ComponentFixture(remoteRobot, remoteComponent)

/**
 * Function, which looks for the StripeButton.
 */
fun ContainerFixture.stripeButton(locator: Locator): StripeButtonFixture {
    return find(locator, Duration.ofSeconds(60))
}

/**
 * This class represents the StripeButton.
 */
@FixtureName("StripeButton")
open class StripeButtonFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ComponentFixture(remoteRobot, remoteComponent)

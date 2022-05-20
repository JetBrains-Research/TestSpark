package nl.tudelft.ewi.se.ciselab.testgenie.uiTest.customfixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.Locator
import java.time.Duration

/**
 * This file contains the custom fixtures used for UI testing.
 */
@FixtureName("JSpinner")
open class JSpinnerFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ComponentFixture(remoteRobot, remoteComponent) {

}

fun ContainerFixture.jSpinner(locator: Locator): JSpinnerFixture {
    return find(locator, Duration.ofSeconds(60))
}

@FixtureName("BasicArrowButton")
open class BasicArrowButtonFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ComponentFixture(remoteRobot, remoteComponent)

fun ContainerFixture.basicArrowButton(locator: Locator): BasicArrowButtonFixture {
    return find(locator, Duration.ofSeconds(60))
}

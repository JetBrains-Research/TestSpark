/**
 * This file contains the custom fixtures used for UI testing.
 */

package org.jetbrains.research.testspark.uiTest.customfixtures

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.Locator
import java.time.Duration

/**
 * This fixture represents a jspinner.
 *
 * @param remoteRobot the remote robot
 * @param remoteComponent the remote component
 */
@FixtureName("JSpinner")
open class JSpinnerFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent,
) : ComponentFixture(remoteRobot, remoteComponent)

/**
 * Locates and returns the jspinner.
 *
 * @param locator the locator with the x-path
 * @return the found jspinner
 */
fun ContainerFixture.jSpinner(locator: Locator): JSpinnerFixture = find(locator, Duration.ofSeconds(60))

/**
 * This fixture represents the arrows on combo-boxes and jspinners.
 *
 * @param remoteRobot the remote robot
 * @param remoteComponent the remote component
 */
@FixtureName("BasicArrowButton")
open class BasicArrowButtonFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent,
) : ComponentFixture(remoteRobot, remoteComponent)

/**
 * Locates and returns the basic arrow button.
 *
 * @param locator the locator with the x-path
 * @return the found basic arrow button
 */
fun ContainerFixture.basicArrowButton(locator: Locator): BasicArrowButtonFixture = find(locator, Duration.ofSeconds(60))

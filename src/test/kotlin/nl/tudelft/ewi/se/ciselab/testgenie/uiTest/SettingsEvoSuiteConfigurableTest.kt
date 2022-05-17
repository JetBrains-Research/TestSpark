package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsEvoSuiteComponent
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsEvoSuiteConfigurable
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsService
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState
import org.assertj.core.api.Assertions.assertThat
import java.util.stream.Stream

class SettingsEvoSuiteConfigurableTest : BasePlatformTestCase() {
    private val settingsConfigurable = SettingsEvoSuiteConfigurable()
    private lateinit var settingsComponent: SettingsEvoSuiteComponent
    private lateinit var settingsState: TestGenieSettingsState

    override fun setUp() {
        super.setUp()
        settingsConfigurable.createComponent()
        settingsComponent = settingsConfigurable.settingsComponent!!
        settingsState = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
    }

    fun test() {
        testIsModifiedCheckBoxes(checkBoxGenerator())
    }

    /**
     * Helper method to test if isModified is working correctly when modifying checkbox values.
     *
     * @param functions stream of expressions to execute
     */
    private fun testIsModifiedCheckBoxes(functions: Stream<() -> Unit>) {
        for (function in functions) {
            function()
            assertThat(settingsConfigurable.isModified).isTrue
            function()
        }
    }

    /**
     * Helper method to generate the stream of functions.
     *
     * @return stream of functions
     */
    private fun checkBoxGenerator(): Stream<() -> Unit> {
        return Stream.of(
            { settingsComponent.assertions = !settingsComponent.assertions },
            { settingsComponent.junitCheck = !settingsComponent.junitCheck },
            { settingsComponent.clientOnThread = !settingsComponent.clientOnThread },
            { settingsComponent.criterionBranch = !settingsComponent.criterionBranch },
            { settingsComponent.criterionCBranch = !settingsComponent.criterionCBranch },
            { settingsComponent.criterionException = !settingsComponent.criterionException },
            { settingsComponent.criterionLine = !settingsComponent.criterionLine },
            { settingsComponent.criterionMethod = !settingsComponent.criterionMethod },
            { settingsComponent.criterionMethodNoException = !settingsComponent.criterionMethodNoException },
            { settingsComponent.criterionOutput = !settingsComponent.criterionOutput },
            { settingsComponent.criterionWeakMutation = !settingsComponent.criterionWeakMutation },
            { settingsComponent.minimize = !settingsComponent.minimize },
            { settingsComponent.sandbox = !settingsComponent.sandbox }
        )
    }
}

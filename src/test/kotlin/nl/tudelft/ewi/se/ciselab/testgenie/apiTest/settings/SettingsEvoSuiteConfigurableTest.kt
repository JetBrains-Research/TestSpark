package nl.tudelft.ewi.se.ciselab.testgenie.uiTest

import com.intellij.openapi.application.ApplicationManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import nl.tudelft.ewi.se.ciselab.testgenie.settings.ContentDigestAlgorithm
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsEvoSuiteComponent
import nl.tudelft.ewi.se.ciselab.testgenie.settings.SettingsEvoSuiteConfigurable
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsService
import nl.tudelft.ewi.se.ciselab.testgenie.settings.TestGenieSettingsState
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.util.stream.Stream

class SettingsEvoSuiteConfigurableTest {
    private val settingsConfigurable = SettingsEvoSuiteConfigurable()
    private lateinit var settingsComponent: SettingsEvoSuiteComponent
    private lateinit var settingsState: TestGenieSettingsState

    override fun setUp() {
        super.setUp()
        settingsConfigurable.createComponent()
        settingsConfigurable.reset()
        settingsComponent = settingsConfigurable.settingsComponent!!
        settingsState = ApplicationManager.getApplication().getService(TestGenieSettingsService::class.java).state
    }

    fun testIsModifiedSeed() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "7"
        assertThat(settingsConfigurable.isModified).isTrue
        settingsComponent.seed = oldValue
    }

    fun testResetSeed() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "5"
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.seed)
    }

    fun testApplySeedCorrect() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "3"
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.seed)
        assertThat(oldValue).isNotEqualTo(settingsState.seed)
        settingsComponent.seed = oldValue
    }

    fun testApplySeedIncorrect() {
        val oldValue = settingsComponent.seed
        settingsComponent.seed = "not a number"
        assertThatThrownBy { settingsConfigurable.apply() }.isInstanceOf(RuntimeException::class.java)
        settingsComponent.seed = oldValue
    }

    fun testIsModifiedConfigurationId() {
        val oldValue = settingsComponent.configurationId
        settingsComponent.configurationId = "7id"
        assertThat(settingsConfigurable.isModified).isTrue
        settingsComponent.configurationId = oldValue
    }

    fun testResetConfigurationId() {
        val oldValue = settingsComponent.configurationId
        settingsComponent.configurationId = "5id"
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.configurationId)
    }

    fun testApplyConfigurationId() {
        val oldValue = settingsComponent.configurationId
        settingsComponent.configurationId = "3id"
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.configurationId)
        assertThat(oldValue).isNotEqualTo(settingsState.configurationId)
        settingsComponent.configurationId = oldValue
    }

    fun testResetAlgorithmComboBox() {
        val oldValue = settingsComponent.algorithm
        settingsComponent.algorithm = ContentDigestAlgorithm.NSGAII
        settingsConfigurable.reset()
        assertThat(oldValue).isEqualTo(settingsComponent.algorithm)
    }

    fun testApplyAlgorithmComboBox() {
        val oldValue = settingsComponent.algorithm
        settingsComponent.algorithm = ContentDigestAlgorithm.BREEDER_GA
        settingsConfigurable.apply()
        assertThat(oldValue).isNotEqualTo(settingsComponent.algorithm)
        assertThat(oldValue).isNotEqualTo(settingsState.algorithm)
        settingsComponent.algorithm = oldValue
    }

    fun testIsModifiedAlgorithmComboBox() {
        val oldValue = settingsComponent.algorithm
        settingsComponent.algorithm = ContentDigestAlgorithm.DYNAMOSA
        assertThat(settingsConfigurable.isModified).isTrue
        settingsComponent.algorithm = oldValue
    }

    fun testIsModifiedCheckBoxes() {
        helperIsModifiedCheckBoxes(checkBoxGenerator())
    }

    fun testResetCheckBoxes() {
        helperResetCheckBoxes(checkBoxGenerator())
    }

    fun testApplyCheckBoxes() {
        helperApplyCheckBoxes(checkBoxGenerator())
    }

    /**
     * Helper method to test if Reset is working correctly when modifying checkbox values.
     *
     * @param functions stream of expressions to execute
     */
    private fun helperResetCheckBoxes(functions: Stream<() -> Triple<() -> Unit, () -> Boolean, () -> Boolean>>) {
        for (function in functions) {
            val triple = function()
            triple.first()
            settingsConfigurable.reset()
            assertThat(triple.second()).isTrue
            assertThat(triple.third()).isTrue
        }
    }

    /**
     * Helper method to test if Apply is working correctly when modifying checkbox values.
     *
     * @param functions stream of expressions to execute
     */
    private fun helperApplyCheckBoxes(functions: Stream<() -> Triple<() -> Unit, () -> Boolean, () -> Boolean>>) {
        for (function in functions) {
            val triple = function()
            triple.first()
            settingsConfigurable.apply()
            assertThat(triple.second()).isFalse
            assertThat(triple.third()).isFalse
        }
    }

    /**
     * Helper method to test if isModified is working correctly when modifying checkbox values.
     *
     * @param functions stream of expressions to execute
     */
    private fun helperIsModifiedCheckBoxes(functions: Stream<() -> Triple<() -> Unit, () -> Boolean, () -> Boolean>>) {
        for (function in functions) {
            val triple = function()
            triple.first()
            assertThat(settingsConfigurable.isModified).isTrue
            assertThat(triple.second()).isFalse
            assertThat(triple.third()).isTrue
        }
    }

    /**
     * Helper method to generate the stream of functions.
     *
     * @return stream of functions
     */
    private fun checkBoxGenerator(): Stream<() -> Triple<() -> Unit, () -> Boolean, () -> Boolean>> {
        return Stream.of(
            {
                val oldValue = settingsComponent.junitCheck
                return@of Triple(
                    { settingsComponent.junitCheck = !settingsComponent.junitCheck },
                    { settingsComponent.junitCheck == oldValue },
                    { settingsState.junitCheck == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.assertions
                return@of Triple(
                    { settingsComponent.assertions = !settingsComponent.assertions },
                    { settingsComponent.assertions == oldValue },
                    { settingsState.assertions == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.clientOnThread
                return@of Triple(
                    { settingsComponent.clientOnThread = !settingsComponent.clientOnThread },
                    { settingsComponent.clientOnThread == oldValue },
                    { settingsState.clientOnThread == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionBranch
                return@of Triple(
                    { settingsComponent.criterionBranch = !settingsComponent.criterionBranch },
                    { settingsComponent.criterionBranch == oldValue },
                    { settingsState.criterionBranch == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionCBranch
                return@of Triple(
                    { settingsComponent.criterionCBranch = !settingsComponent.criterionCBranch },
                    { settingsComponent.criterionCBranch == oldValue },
                    { settingsState.criterionCBranch == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionException
                return@of Triple(
                    { settingsComponent.criterionException = !settingsComponent.criterionException },
                    { settingsComponent.criterionException == oldValue },
                    { settingsState.criterionException == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionLine
                return@of Triple(
                    { settingsComponent.criterionLine = !settingsComponent.criterionLine },
                    { settingsComponent.criterionLine == oldValue },
                    { settingsState.criterionLine == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionMethod
                return@of Triple(
                    { settingsComponent.criterionMethod = !settingsComponent.criterionMethod },
                    { settingsComponent.criterionMethod == oldValue },
                    { settingsState.criterionMethod == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionMethodNoException
                return@of Triple(
                    { settingsComponent.criterionMethodNoException = !settingsComponent.criterionMethodNoException },
                    { settingsComponent.criterionMethodNoException == oldValue },
                    { settingsState.criterionMethodNoException == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionOutput
                return@of Triple(
                    { settingsComponent.criterionOutput = !settingsComponent.criterionOutput },
                    { settingsComponent.criterionOutput == oldValue },
                    { settingsState.criterionOutput == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.criterionWeakMutation
                return@of Triple(
                    { settingsComponent.criterionWeakMutation = !settingsComponent.criterionWeakMutation },
                    { settingsComponent.criterionWeakMutation == oldValue },
                    { settingsState.criterionWeakMutation == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.minimize
                return@of Triple(
                    { settingsComponent.minimize = !settingsComponent.minimize },
                    { settingsComponent.minimize == oldValue },
                    { settingsState.minimize == oldValue }
                )
            },
            {
                val oldValue = settingsComponent.sandbox
                return@of Triple(
                    { settingsComponent.sandbox = !settingsComponent.sandbox },
                    { settingsComponent.sandbox == oldValue },
                    { settingsState.sandbox == oldValue }
                )
            }
        )
    }
}

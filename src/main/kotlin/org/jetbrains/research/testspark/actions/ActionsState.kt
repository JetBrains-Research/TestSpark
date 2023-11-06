package org.jetbrains.research.testspark.actions

import org.jetbrains.research.testspark.TestSparkDefaultsBundle
import org.jetbrains.research.testspark.data.ContentDigestAlgorithm

data class ActionsState(
    var llmUserToken: String = DefaultActionsState.llmUserToken,
    var model: String = DefaultActionsState.model,
    var llmPlatform: String = DefaultActionsState.llmPlatform,
    var javaPath: String = DefaultActionsState.javaPath,
    var algorithm: ContentDigestAlgorithm = DefaultActionsState.algorithm,
) {

    /**
     * Default values of ActionsState.
     */
    object DefaultActionsState {
        val llmUserToken: String = TestSparkDefaultsBundle.defaultValue("llmToken")
        var model: String = TestSparkDefaultsBundle.defaultValue("model")
        var llmPlatform: String = TestSparkDefaultsBundle.defaultValue("llmPlatform")
        var javaPath: String = TestSparkDefaultsBundle.defaultValue("javaPath")
        val algorithm: ContentDigestAlgorithm = ContentDigestAlgorithm.DYNAMOSA
    }
}

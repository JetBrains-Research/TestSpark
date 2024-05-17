package org.jetbrains.research.testspark.data

import org.jetbrains.research.testspark.collectors.FeedbackSentCollector
import org.jetbrains.research.testspark.collectors.LikedDislikedCollector
import org.jetbrains.research.testspark.collectors.CoverageStatusShowedCollector
import org.jetbrains.research.testspark.collectors.TestGenerationStartedCollector
import org.jetbrains.research.testspark.collectors.TestGenerationFinishedCollector
import org.jetbrains.research.testspark.collectors.GeneratedTestsCollector
import org.jetbrains.research.testspark.collectors.IntegratedTestsCollector

class CollectorsData {
    var id: String? = null

    var testGenerationStartTime: Long? = null

    var technique: Technique? = null

    var codeType: CodeType? = null

    val feedbackSentCollector = FeedbackSentCollector()

    val likedDislikedCollector = LikedDislikedCollector()

    val coverageStatusShowedCollector = CoverageStatusShowedCollector()

    val testGenerationStartedCollector = TestGenerationStartedCollector()

    val testGenerationFinishedCollector = TestGenerationFinishedCollector()

    val generatedTestsCollector = GeneratedTestsCollector()

    val integratedTestsCollector = IntegratedTestsCollector()
}

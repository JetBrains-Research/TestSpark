package org.jetbrains.research.testspark.collectors.data

import org.jetbrains.research.testspark.collectors.CoverageStatusShowedCollector
import org.jetbrains.research.testspark.collectors.FeedbackSentCollector
import org.jetbrains.research.testspark.collectors.GeneratedTestsCollector
import org.jetbrains.research.testspark.collectors.IntegratedTestsCollector
import org.jetbrains.research.testspark.collectors.LikedDislikedCollector
import org.jetbrains.research.testspark.collectors.TestGenerationFinishedCollector
import org.jetbrains.research.testspark.collectors.TestGenerationStartedCollector

/**
 * This class represents a container for different user experience event collectors.
 */
class UserExperienceCollectors {
    val feedbackSentCollector = FeedbackSentCollector()

    val likedDislikedCollector = LikedDislikedCollector()

    val coverageStatusShowedCollector = CoverageStatusShowedCollector()

    val testGenerationStartedCollector = TestGenerationStartedCollector()

    val testGenerationFinishedCollector = TestGenerationFinishedCollector()

    val generatedTestsCollector = GeneratedTestsCollector()

    val integratedTestsCollector = IntegratedTestsCollector()
}

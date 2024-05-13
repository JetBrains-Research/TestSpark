package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import org.jetbrains.research.testspark.collectors.CoverageStatusShowedCollector
import org.jetbrains.research.testspark.collectors.FeedbackSentCollector
import org.jetbrains.research.testspark.collectors.GeneratedTestsCollector
import org.jetbrains.research.testspark.collectors.IntegratedTestsCollector
import org.jetbrains.research.testspark.collectors.LikedDislikedCollector
import org.jetbrains.research.testspark.collectors.TestGenerationFinishedCollector
import org.jetbrains.research.testspark.collectors.TestGenerationStartedCollector
import org.jetbrains.research.testspark.data.CollectorsData

@Service(Service.Level.PROJECT)
class CollectorService {
    val feedbackSentCollector = FeedbackSentCollector()
    val likedDislikedCollector = LikedDislikedCollector()
    val coverageStatusShowedCollector = CoverageStatusShowedCollector()
    val testGenerationStartedCollector = TestGenerationStartedCollector()
    val testGenerationFinishedCollector = TestGenerationFinishedCollector()
    val generatedTestsCollector = GeneratedTestsCollector()
    val integratedTestsCollector = IntegratedTestsCollector()

    val data: CollectorsData = CollectorsData()
}

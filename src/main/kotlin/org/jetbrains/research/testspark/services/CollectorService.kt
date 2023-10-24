package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import org.jetbrains.research.testspark.collectors.CoverageStatusShowedCollector
import org.jetbrains.research.testspark.collectors.FeedbackSentCollector
import org.jetbrains.research.testspark.collectors.LikedDislikedCollector

@Service(Service.Level.PROJECT)
class CollectorService {
    val feedbackSentCollector = FeedbackSentCollector()
    val likedDislikedCollector = LikedDislikedCollector()
    val coverageStatusShowedCollector = CoverageStatusShowedCollector()
}

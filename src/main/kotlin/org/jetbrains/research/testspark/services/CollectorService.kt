package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import org.jetbrains.research.testspark.collectors.FeedbackSentCollector

@Service(Service.Level.PROJECT)
class CollectorService {
    val feedbackSentCollector = FeedbackSentCollector()
}

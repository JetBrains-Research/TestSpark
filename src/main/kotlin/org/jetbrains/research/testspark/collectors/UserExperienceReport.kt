package org.jetbrains.research.testspark.collectors

import org.jetbrains.research.testspark.core.test.data.CodeType
import org.jetbrains.research.testspark.tools.GenerationTool

class UserExperienceReport(generationTool: GenerationTool) {
    // Test generation id
    var id: String? = null

    // Test generation starting time
    var testGenerationStartTime: Long? = null

    // Tool used in the test generation
    var generationTool: GenerationTool = generationTool

    // Code type tested in the test generation
    var codeType: CodeType? = null

    val feedbackSentCollector = FeedbackSentCollector()

    val likedDislikedCollector = LikedDislikedCollector()
}

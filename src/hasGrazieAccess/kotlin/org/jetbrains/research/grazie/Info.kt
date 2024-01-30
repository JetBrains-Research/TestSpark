package org.jetbrains.research.grazie

import org.jetbrains.research.testSpark.grazie.TestGeneration
import org.jetbrains.research.testspark.tools.llm.generation.Info

class Info : Info {
    override fun availableProfiles(): Set<String> = TestGeneration.availableProfiles
}

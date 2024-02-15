package org.jetbrains.research.grazie

import org.jetbrains.research.testSpark.grazie.TestGeneration
import org.jetbrains.research.testspark.tools.llm.generation.grazie.GrazieInfo

class Info : GrazieInfo {
    override fun availableProfiles(): Set<String> = TestGeneration.availableProfiles
}

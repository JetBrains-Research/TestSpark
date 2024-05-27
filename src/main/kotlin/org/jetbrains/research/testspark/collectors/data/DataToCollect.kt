package org.jetbrains.research.testspark.collectors.data

import org.jetbrains.research.testspark.data.CodeType

/**
 * Class representing the data to be collected.
 */
class DataToCollect {
    // Test generation id
    var id: String? = null

    // Test generation starting time
    var testGenerationStartTime: Long? = null

    // Technique used in the test generation
    var technique: Technique? = null

    // Code type tested in the test generation
    var codeType: CodeType? = null
}

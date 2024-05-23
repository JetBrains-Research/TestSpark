package org.jetbrains.research.testspark.collectors

/**
 * Helper class for collecting data with regular expressions.
 */
class CollectorsHelper {

    val sessionIDRegex = """^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$""".toRegex()
    val testIDRegex =
        """^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}_[0-9]{1,2}$""".toRegex()
}

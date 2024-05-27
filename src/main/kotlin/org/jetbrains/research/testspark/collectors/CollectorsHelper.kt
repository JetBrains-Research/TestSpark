package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EnumEventField
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.eventLog.events.IntEventField
import org.jetbrains.research.testspark.data.CodeType
import org.jetbrains.research.testspark.data.Technique

/**
 * Helper class for working with collectors and event logging.
 */
object CollectorsHelper {
    /**
     * The version number of the event log group.
     */
    private val EVENT_LOG_GROUP_VERSION: Int = 1

    /**
     * Regular expression pattern used to validate session IDs.
     *
     * The pattern follows the UUID format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,
     * where each 'x' represents a hexadecimal digit (0-9, a-f, A-F).
     */
    private val sessionIDRegex =
        """^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$""".toRegex()

    /**
     * Regular expression pattern to validate test IDs.
     *
     * This pattern is used to validate a test ID, which consists of two parts separated by an underscore:
     * - The first part is a UUID with the following format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx,
     *   where each 'x' represents a hexadecimal digit (0-9, a-f, A-F).
     * - The second part is a number between 1 and 99, inclusive.
     */
    private val testIDRegex =
        """^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}_[0-9]{1,2}$""".toRegex()

    /**
     * Returns the group ID for the tests set group.
     */
    fun getTestsSetGroupID(): String = "tests.set"

    /**
     * Returns the group ID for the tests coverage group.
     */
    fun getTestsCoverageGroupID(): String = "tests.coverage"

    /**
     * Returns the group ID for individual tests.
     */
    fun getIndividualTestsGroupID(): String = "individual.tests"

    /**
     * Returns the EventLogGroup object for the given groupId.
     */
    fun getGroup(groupId: String) = EventLogGroup(groupId, EVENT_LOG_GROUP_VERSION)

    /**
     * Returns the count of an event field.
     */
    fun getCount(): IntEventField = IntEventField("count")

    /**
     * Returns the technique field for an event.
     */
    fun getTechnique(): EnumEventField<Technique> = EventFields.Enum("technique", Technique::class.java)

    /**
     * Returns the EnumEventField instance representing the code type for which test generation was performed.
     */
    fun getLevel(): EnumEventField<CodeType> = EventFields.Enum("level", CodeType::class.java)

    /**
     * Returns the test ID for an event.
     */
    fun getTestID() = EventFields.StringValidatedByRegexp("id", testIDRegex.pattern)

    /**
     * Returns the session ID for the event.
     */
    fun getSessionID() = EventFields.StringValidatedByRegexp("id", sessionIDRegex.pattern)
}

package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.eventLog.EventLogGroup
import com.intellij.internal.statistic.eventLog.events.EventFields
import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

/**
 * This class represents a collector for tracking the event of coverage status being showed.
 *
 * @property sessionId the EventFields.StringValidatedByRegexp instance representing the session ID for the event
 */
class CoverageStatusShowedCollector : CounterUsagesCollector() {
    private val groupId = "tests.coverage"
    private val group = EventLogGroup(groupId, 1)

    private val eventId = "coverage.status.showed"
    private val sessionId = EventFields.StringValidatedByRegexp("id", CollectorsHelper().sessionIDRegex.pattern)

    private val event = group.registerEvent(eventId, sessionId)

    override fun getGroup() = group

    fun logEvent(sessionId: String) {
        event.log(sessionId)
    }
}

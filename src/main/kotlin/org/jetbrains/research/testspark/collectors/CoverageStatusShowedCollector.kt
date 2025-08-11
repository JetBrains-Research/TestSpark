package org.jetbrains.research.testspark.collectors

import com.intellij.internal.statistic.service.fus.collectors.CounterUsagesCollector

/**
 * This class represents a collector for tracking the event of coverage status being shown.
 *
 * @property sessionId the EventFields.StringValidatedByRegexp instance representing the session ID for the event
 */
class CoverageStatusShowedCollector : CounterUsagesCollector() {
    private val groupId = CollectorsHelper.getTestsCoverageGroupID()
    private val group = CollectorsHelper.getGroup(groupId)

    private val eventId = "coverage.status.showed"
    private val sessionId = CollectorsHelper.getSessionID()

    private val event = group.registerEvent(eventId, sessionId)

    override fun getGroup() = group

    fun logEvent(sessionId: String) {
        event.log(sessionId)
    }
}

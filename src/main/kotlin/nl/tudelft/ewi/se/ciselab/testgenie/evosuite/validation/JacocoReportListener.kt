package nl.tudelft.ewi.se.ciselab.testgenie.evosuite.validation

import com.intellij.util.messages.Topic

val JACOCO_REPORT_TOPIC: Topic<JacocoReportListener> = Topic.create(
    "JACOCO_REPORT_TOPIC", JacocoReportListener::class.java, Topic.BroadcastDirection.TO_PARENT
)

/**
 * Topic interface for sending and receiving results of jacoco test coverage calculation
 *
 * Subscribers to this topic will receive information about the coverage of the user's current test cases
 */
interface JacocoReportListener {
    fun receiveJacocoReport(coverages: Validator.CoverageLineByLine)
}

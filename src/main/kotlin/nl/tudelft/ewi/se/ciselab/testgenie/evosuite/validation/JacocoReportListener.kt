package nl.tudelft.ewi.se.ciselab.testgenie.evosuite.validation

import com.intellij.util.messages.Topic

val JACOCO_REPORT_TOPIC: Topic<JacocoReportListener> = Topic.create(
    "JACOCO_REPORT_TOPIC", JacocoReportListener::class.java, Topic.BroadcastDirection.TO_PARENT
)

interface JacocoReportListener {
    fun receiveJacocoReport(coverages: Validator.CoverageLineByLine)
}

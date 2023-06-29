package org.jetbrains.research.testgenie.data

import org.evosuite.utils.CompactTestCase

data class Report(var testCaseList: HashMap<String, CompactTestCase>)

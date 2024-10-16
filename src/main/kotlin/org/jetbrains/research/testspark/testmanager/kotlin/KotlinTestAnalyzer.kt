package org.jetbrains.research.testspark.testmanager.kotlin

import org.jetbrains.research.testspark.testmanager.template.TestAnalyzer

object KotlinTestAnalyzer : TestAnalyzer {

    override fun extractFirstTestMethodCode(classCode: String): String {
        val testMethods = StringBuilder()
        val lines = classCode.lines()

        var methodStarted = false
        var balanceOfBrackets = 0

        for (line in lines) {
            if (!methodStarted && line.contains("@Test")) {
                methodStarted = true
                testMethods.append(line).append("\n")
            } else if (methodStarted) {
                testMethods.append(line).append("\n")
                for (char in line) {
                    if (char == '{') {
                        balanceOfBrackets++
                    } else if (char == '}') {
                        balanceOfBrackets--
                    }
                }
                if (balanceOfBrackets == 0) {
                    methodStarted = false
                    testMethods.append("\n")
                }
            }
        }

        return testMethods.toString()
    }

    override fun extractFirstTestMethodName(oldTestCaseName: String, classCode: String): String {
        val lines = classCode.lines()
        var testMethodName = oldTestCaseName

        for (line in lines) {
            if (line.contains("@Test")) {
                val methodDeclarationLine = lines[lines.indexOf(line) + 1]
                val matchResult = Regex("fun\\s+(\\w+)\\s*\\(").find(methodDeclarationLine)
                if (matchResult != null) {
                    testMethodName = matchResult.groupValues[1]
                }
                break
            }
        }
        return testMethodName
    }

    override fun getClassFromTestCaseCode(code: String): String {
        val pattern = Regex("class\\s+(\\S+)\\s*\\{")
        val matchResult = pattern.find(code)
        matchResult ?: return "GeneratedTest"
        val (className) = matchResult.destructured
        return className
    }

    override fun getFileNameFromTestCaseCode(code: String): String {
        return "${getClassFromTestCaseCode(code)}.kt"
    }
}

package org.jetbrains.research.testspark.testmanager.java

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import org.jetbrains.research.testspark.testmanager.template.TestAnalyzer

object JavaTestAnalyzer : TestAnalyzer {

    override fun extractFirstTestMethodCode(classCode: String): String {
        var result = ""
        try {
            val componentUnit: CompilationUnit = StaticJavaParser.parse(classCode)
            object : VoidVisitorAdapter<Any?>() {
                override fun visit(method: MethodDeclaration, arg: Any?) {
                    super.visit(method, arg)
                    if (method.getAnnotationByName("Test").isPresent) {
                        result += "\t" + method.toString().replace("\n", "\n\t") + "\n\n"
                    }
                }
            }.visit(componentUnit, null)

            return result
        } catch (e: ParseProblemException) {
            val upperCutCode = "\t@Test" + classCode.split("@Test").last()
            var methodStarted = false
            var balanceOfBrackets = 0
            for (symbol in upperCutCode) {
                result += symbol
                if (symbol == '{') {
                    methodStarted = true
                    balanceOfBrackets++
                }
                if (symbol == '}') {
                    balanceOfBrackets--
                }
                if (methodStarted && balanceOfBrackets == 0) {
                    break
                }
            }
            return result + "\n"
        }
    }

    override fun extractFirstTestMethodName(oldTestCaseName: String, classCode: String): String {
        var result = ""
        try {
            val componentUnit: CompilationUnit = StaticJavaParser.parse(classCode)

            object : VoidVisitorAdapter<Any?>() {
                override fun visit(method: MethodDeclaration, arg: Any?) {
                    super.visit(method, arg)
                    if (method.getAnnotationByName("Test").isPresent) {
                        result = method.nameAsString
                    }
                }
            }.visit(componentUnit, null)

            return result
        } catch (e: ParseProblemException) {
            return oldTestCaseName
        }
    }

    override fun getClassFromTestCaseCode(code: String): String {
        val pattern = Regex("public\\s+class\\s+(\\S+)\\s*\\{")
        val matchResult = pattern.find(code)
        matchResult ?: return "GeneratedTest"
        val (className) = matchResult.destructured
        return className
    }

    override fun getFileNameFromTestCaseCode(code: String): String {
        return "${getClassFromTestCaseCode(code)}.java"
    }
}

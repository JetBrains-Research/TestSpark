package org.jetbrains.research.testspark.helpers.kotlin

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.helpers.TestClassBuilderHelper
import java.io.File

object KotlinClassBuilderHelper : TestClassBuilderHelper {

    private val log = Logger.getInstance(this::class.java)

    override fun generateCode(
        project: Project,
        className: String,
        body: String,
        imports: Set<String>,
        packageString: String,
        runWith: String,
        otherInfo: String,
        testGenerationData: TestGenerationData,
    ): String {
        log.debug("[KotlinClassBuilderHelper] Generate code for $className")

        var testFullText = printUpperPart(className, imports, packageString, runWith, otherInfo)

        // Add each test (exclude expected exception)
        testFullText += body

        // close the test class
        testFullText += "}"

        testFullText.replace("\r\n", "\n")

        // Reduce the number of line breaks for better readability
        return formatCode(project, Regex("\n\n\n(?:\n)*").replace(testFullText, "\n\n"), testGenerationData)
    }

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

    override fun formatCode(project: Project, code: String, generatedTestData: TestGenerationData): String {
        var result = ""
        WriteCommandAction.runWriteCommandAction(project) {
            val fileName = generatedTestData.resultPath + File.separatorChar + "Formatted.kt"
            // Create a temporary PsiFile
            val psiFile: PsiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(fileName, KotlinLanguage.INSTANCE, code)

            CodeStyleManager.getInstance(project).reformat(psiFile)

            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            result = document?.text ?: code

            File(fileName).delete()
        }
        log.info("Formatted result class: $result")
        return result
    }

    private fun printUpperPart(
        className: String,
        imports: Set<String>,
        packageString: String,
        runWith: String,
        otherInfo: String,
    ): String {
        var testText = ""

        // Add package
        if (packageString.isNotBlank()) {
            testText += "package $packageString\n"
        }

        // Add imports
        imports.forEach { importedElement ->
            testText += "$importedElement\n"
        }

        testText += "\n"

        // Add runWith if exists
        if (runWith.isNotBlank()) {
            testText += "@RunWith($runWith::class)\n"
        }

        // Open the test class
        testText += "class $className {\n\n"

        // Add other presets (annotations, non-test functions)
        if (otherInfo.isNotBlank()) {
            testText += otherInfo
        }

        return testText
    }
}

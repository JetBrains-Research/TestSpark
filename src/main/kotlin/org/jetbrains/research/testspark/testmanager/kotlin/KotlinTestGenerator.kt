package org.jetbrains.research.testspark.testmanager.kotlin

import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.testmanager.template.TestGenerator
import java.io.File

object KotlinTestGenerator : TestGenerator {

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

        var testFullText =
            printUpperPart(className, imports, packageString, runWith, otherInfo)

        // Add each test (exclude expected exception)
        testFullText += body

        // close the test class
        testFullText += "}"

        testFullText.replace("\r\n", "\n")

        // Reduce the number of line breaks for better readability
        return formatCode(project, Regex("\n\n\n(?:\n)*").replace(testFullText, "\n\n"), testGenerationData)
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

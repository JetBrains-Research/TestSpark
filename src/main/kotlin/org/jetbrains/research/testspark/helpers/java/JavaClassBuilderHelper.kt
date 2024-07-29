package org.jetbrains.research.testspark.helpers.java

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.codeStyle.CodeStyleManager
import org.jetbrains.research.testspark.core.data.TestGenerationData
import org.jetbrains.research.testspark.helpers.TestClassBuilderHelper
import java.io.File

object JavaClassBuilderHelper : TestClassBuilderHelper {

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
        var testFullText = printUpperPart(className, imports, packageString, runWith, otherInfo)

        // Add each test (exclude expected exception)
        testFullText += body

        // close the test class
        testFullText += "}"

        testFullText.replace("\r\n", "\n")

        /**
         * for better readability and make the tests shorter, we reduce the number of line breaks:
         *  when we have three or more sequential \n, reduce it to two.
         */
        return formatCode(project, Regex("\n\n\n(?:\n)*").replace(testFullText, "\n\n"), testGenerationData)
    }

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

    override fun formatCode(project: Project, code: String, generatedTestData: TestGenerationData): String {
        var result = ""
        WriteCommandAction.runWriteCommandAction(project) {
            val fileName = generatedTestData.resultPath + File.separatorChar + "Formatted.java"
            // create a temporary PsiFile
            val psiFile: PsiFile = PsiFileFactory.getInstance(project)
                .createFileFromText(
                    fileName,
                    JavaLanguage.INSTANCE,
                    code,
                )

            CodeStyleManager.getInstance(project).reformat(psiFile)

            val document = PsiDocumentManager.getInstance(project).getDocument(psiFile)
            result = document?.text ?: code

            File(fileName).delete()
        }

        return result
    }

    /**
     * Returns the upper part of test suite (package name, imports, and test class name) as a string.
     *
     * @return the upper part of test suite (package name, imports, and test class name) as a string.
     */
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
            testText += "package $packageString;\n"
        }

        // add imports
        imports.forEach { importedElement ->
            testText += "$importedElement\n"
        }

        testText += "\n"

        // add runWith if exists
        if (runWith.isNotBlank()) {
            testText += "@RunWith($runWith)\n"
        }
        // open the test class
        testText += "public class $className {\n\n"

        // Add other presets (annotations, non-test functions)
        if (otherInfo.isNotBlank()) {
            testText += otherInfo
        }

        return testText
    }
}

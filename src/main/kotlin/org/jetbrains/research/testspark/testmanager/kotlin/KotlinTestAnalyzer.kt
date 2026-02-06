package org.jetbrains.research.testspark.testmanager.kotlin

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.containingClass
import org.jetbrains.research.testspark.core.test.data.TestSample
import org.jetbrains.research.testspark.kotlin.KotlinPsiMethodWrapper
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

    override fun extractFirstTestMethodName(
        oldTestCaseName: String,
        classCode: String,
    ): String {
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

    override fun getFileNameFromTestCaseCode(code: String): String = "${getClassFromTestCaseCode(code)}.kt"

    override fun getTestSamplesList(
        project: Project,
        file: VirtualFile,
    ): List<TestSample> {
        val psiFile = PsiManager.getInstance(project).findFile(file) ?: return emptyList()
        val testMethods = mutableListOf<TestSample>()

        psiFile.accept(
            object : KtTreeVisitorVoid() {
                override fun visitNamedFunction(function: KtNamedFunction) {
                    super.visitNamedFunction(function)

                    if (KotlinPsiMethodWrapper(function).isTestingMethod()) {
                        val ktClassName = function.containingClass()?.name
                        val imports = retrieveImportStatements(psiFile as KtFile)
                        testMethods.add(
                            TestSample(createHtmlMethodName(ktClassName, function.name), createTestSampleCode(imports, function.text)),
                        )
                    }
                }
            },
        )

        return testMethods
    }

    /**
     * Retrieves and formats all the import statements from the given Kotlin file.
     *
     * @param psiFile the Kotlin file (KtFile) from which to retrieve the import statements
     * @return a string containing all the import statements, each on a new line, or an empty string if no imports are found
     */
    private fun retrieveImportStatements(psiFile: KtFile): String {
        val importList = psiFile.importList ?: return ""
        return importList.imports.joinToString("\n") { it.text }
    }

    /**
     * Creates a test sample code string by combining the provided import statements
     * with the given method code inside a sample test class.
     *
     * @param imports the necessary import statements formatted as a string
     * @param methodCode the code for a method to be included inside the test class
     * @return a formatted test sample code string containing the imports and method code within the test class
     */
    private fun createTestSampleCode(
        imports: String,
        methodCode: String,
    ): String {
        var normalizedImports = imports
        if (normalizedImports.isNotBlank()) normalizedImports += "\n\n"
        return normalizedImports +
            "class TestSample {\n" +
            "    $methodCode\n" +
            "}"
    }

    override fun isSupportedFileType(file: VirtualFile): Boolean = file.extension?.lowercase() == "kt"
}

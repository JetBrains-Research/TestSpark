package org.jetbrains.research.testspark.testmanager.java

import com.github.javaparser.ParseProblemException
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.visitor.VoidVisitorAdapter
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.util.containers.stream
import org.jetbrains.research.testspark.core.test.data.TestSample
import org.jetbrains.research.testspark.java.JavaPsiMethodWrapper
import org.jetbrains.research.testspark.testmanager.template.TestAnalyzer
import kotlin.collections.indexOf

object JavaTestAnalyzer : TestAnalyzer {
    override fun extractFirstTestMethodCode(classCode: String): String {
        var result = ""
        try {
            val componentUnit: CompilationUnit = StaticJavaParser.parse(classCode)
            object : VoidVisitorAdapter<Any?>() {
                override fun visit(
                    method: MethodDeclaration,
                    arg: Any?,
                ) {
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

    override fun extractFirstTestMethodName(
        oldTestCaseName: String,
        classCode: String,
    ): String {
        var result = ""
        try {
            val componentUnit: CompilationUnit = StaticJavaParser.parse(classCode)

            object : VoidVisitorAdapter<Any?>() {
                override fun visit(
                    method: MethodDeclaration,
                    arg: Any?,
                ) {
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

    override fun getFileNameFromTestCaseCode(code: String): String = "${getClassFromTestCaseCode(code)}.java"

    override fun getTestSamplesList(
        project: Project,
        file: VirtualFile,
    ): List<TestSample> {
        val psiJavaFile = PsiManager.getInstance(project).findFile(file) as PsiJavaFile

        val psiClass =
            psiJavaFile.classes[
                psiJavaFile.classes
                    .stream()
                    .map { it.name }
                    .toArray()
                    .indexOf(psiJavaFile.name.removeSuffix(".java")),
            ]

        val imports = retrieveImportStatements(psiJavaFile, psiClass)
        val testSamplesList = mutableListOf<TestSample>()
        psiClass.allMethods.forEach { method -> processCandidateMethod(method, imports, psiClass, testSamplesList) }

        return testSamplesList
    }

    override fun isSupportedFileType(file: VirtualFile): Boolean {
        val javaFileType: FileType = FileTypeManager.getInstance().getFileTypeByExtension("java")
        return file.fileType === javaFileType
    }

    /**
     * Retrieves the import statements for a {@link PsiJavaFile} and {@link PsiClass}.
     *
     * @param psiJavaFile The PSI Java file object.
     * @param psiClass The PSI class object.
     * @return A string of import statements.
     */
    private fun retrieveImportStatements(
        psiJavaFile: PsiJavaFile,
        psiClass: PsiClass,
    ): String {
        var imports =
            psiJavaFile.importList
                ?.allImportStatements
                ?.map { it.text }
                ?.toList()
                ?.joinToString("\n") ?: ""
        if (psiClass.qualifiedName != null && psiClass.qualifiedName!!.contains(".")) {
            imports += "\nimport ${psiClass.qualifiedName?.substringBeforeLast(".") + ".*"};"
        }
        return imports
    }

    /**
     * Processes a {@link PsiMethod} as a candidate.
     *
     * If it is a candidate, i.e., it is annotated as a JUnit test, add it to {@link #testNames} and
     * {@link #initialTestCodes}, respectively.
     *
     * @param psiMethod The PSI method object.
     * @param imports The imports required for the code generation.
     * @param psiClass The PSI class object.
     */
    private fun processCandidateMethod(
        psiMethod: PsiMethod,
        imports: String,
        psiClass: PsiClass,
        testSamplesList: MutableList<TestSample>,
    ) {
        if (JavaPsiMethodWrapper(psiMethod).isTestingMethod()) {
            val code: String = createTestSampleCode(imports, psiMethod.text)
            testSamplesList.add(TestSample(createHtmlMethodName(psiClass.qualifiedName, psiMethod.name), code))
        }
    }

    /**
     * Creates a class from the imports and the method codes as a test sample.
     *
     * @param imports The imports required for the code.
     * @param methodCode The code of the method.
     * @return A class wrapping the given method.
     */
    private fun createTestSampleCode(
        imports: String,
        methodCode: String,
    ): String {
        var normalizedImports = imports
        if (normalizedImports.isNotBlank()) normalizedImports += "\n\n"
        return normalizedImports +
            "public class TestSample {\n" +
            "    $methodCode\n" +
            "}"
    }
}

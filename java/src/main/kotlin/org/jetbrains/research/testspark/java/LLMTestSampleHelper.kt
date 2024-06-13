package org.jetbrains.research.testspark.java

import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiJavaFile
import com.intellij.psi.PsiManager
import com.intellij.psi.PsiMethod
import com.intellij.util.containers.stream

object LLMTestSampleHelper {
    /**
     * Retrieves a list of test samples from the given project.
     *
     * @return A list of strings, representing the names of the test samples.
     */
    fun collectTestSamples(project: Project, testNames: MutableList<String>, initialTestCodes: MutableList<String>) {
        val projectFileIndex: ProjectFileIndex = ProjectRootManager.getInstance(project).fileIndex
        val javaFileType: FileType = FileTypeManager.getInstance().getFileTypeByExtension("java")

        projectFileIndex.iterateContent { file ->
            if (file.fileType === javaFileType) {
                try {
                    val psiJavaFile = (PsiManager.getInstance(project).findFile(file) as PsiJavaFile)
                    val psiClass = psiJavaFile.classes[
                        psiJavaFile.classes.stream().map { it.name }.toArray()
                            .indexOf(psiJavaFile.name.removeSuffix(".java")),
                    ]
                    var imports = psiJavaFile.importList?.allImportStatements?.map { it.text }?.toList()
                        ?.joinToString("\n") ?: ""
                    if (psiClass.qualifiedName != null && psiClass.qualifiedName!!.contains(".")) {
                        imports += "\nimport ${psiClass.qualifiedName?.substringBeforeLast(".") + ".*"};"
                    }
                    psiClass.allMethods.forEach { method ->
                        val annotations = method.modifierList.annotations
                        annotations.forEach { annotation ->
                            if (annotation.qualifiedName == "org.junit.jupiter.api.Test" || annotation.qualifiedName == "org.junit.Test") {
                                val code: String = createTestSampleClass(imports, method.text)
                                testNames.add(createMethodName(psiClass, method))
                                initialTestCodes.add(code)
                            }
                        }
                    }
                } catch (_: Exception) {
                }
            }
            true
        }
    }

    fun createTestSampleClass(imports: String, methodCode: String): String {
        var normalizedImports = imports
        if (normalizedImports.isNotBlank()) normalizedImports += "\n\n"
        return normalizedImports +
            "public class TestSample {\n" +
            "   $methodCode\n" +
            "}"
    }

    private fun createMethodName(psiClass: PsiClass, method: PsiMethod): String =
        "<html>${psiClass.qualifiedName}#${method.name}</html>"
}

package org.jetbrains.research.testspark.display.common

import com.intellij.lang.Language
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.LanguageTextField
import com.intellij.util.LocalTimeCounter

/**
 * This class is responsible for creating a test case document.
 *
 * @constructor Creates a new TestCaseDocumentCreator object.
 */
open class TestCaseDocumentCreator(private val className: String) : LanguageTextField.DocumentCreator {
    /**
     * Creates a document based on the given parameters. Copied from com.intellij.ui.LanguageTextField
     *
     * @param value            The initial text to set in the document.
     * @param language         The language associated with the document.
     * @param project          The project to which the document belongs.
     * @return The created document. Can be null if the language is null and the document
     *         is created using EditorFactory.
     */
    override fun createDocument(value: String?, language: Language?, project: Project?): Document {
        return if (language != null) {
            val notNullProject = project ?: ProjectManager.getInstance().defaultProject
            val factory = PsiFileFactory.getInstance(notNullProject)
            val fileType: FileType = language.associatedFileType!!
            val stamp = LocalTimeCounter.currentTime()
            val psiFile = factory.createFileFromText(
                "$className." + fileType.defaultExtension,
                fileType,
                "",
                stamp,
                true,
                false,
            )
            customizePsiFile(psiFile)

            // No need to guess project in getDocument - we already know it
            val document = PsiDocumentManager.getInstance(notNullProject).getDocument(psiFile)!!
            ApplicationManager.getApplication().runWriteAction {
                document.setText(value!!) // do not put initial value into backing LightVirtualFile.contentsToByteArray
            }
            document
        } else {
            EditorFactory.getInstance().createDocument(value!!)
        }
    }

    /**
     * Customizes the given PsiFile.
     *
     * @param file The PsiFile to be customized.
     */
    open fun customizePsiFile(file: PsiFile?) {}
}

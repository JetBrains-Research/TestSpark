package org.jetbrains.research.testspark.langwrappers

import com.intellij.lang.LanguageExtension
import com.intellij.psi.PsiFile

/**
 * This is the provider interface for a PsiHelper. The PsiHelper allows for
 * custom handling or manipulating PSI (Program Structure Interface) elements.
 */
interface PsiHelperProvider {

    /**
     * Get a PsiHelper for the given file.
     *
     * @param file the PsiFile to get the PsiHelper for.
     * @return a PsiHelper object.
     */
    fun getPsiHelper(file: PsiFile): PsiHelper

    companion object {
        // An extension point that allows for custom PsiHelperProviders to be registered for different languages
        private val EP = LanguageExtension<PsiHelperProvider>("org.jetbrains.research.testspark.psiHelperProvider")

        /**
         * Retrieves a PsiHelper for the given file based on its language.
         *
         * It attempts to get the PsiHelperProvider registered for the specified language.
         * If none exists, the method will return null.
         * Finally, it uses this PsiHelperProvider to get a PsiHelper for the file.
         *
         * @param file The PsiFile to get the PsiHelper for.
         * @return The PsiHelper for the file or null if it couldn't be obtained.
         */
        fun getPsiHelper(file: PsiFile): PsiHelper? {
            val language = file.language
            return EP.forLanguage(language)?.getPsiHelper(file)
        }
    }
}

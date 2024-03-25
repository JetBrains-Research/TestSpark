package org.jetbrains.research.testspark.core.data

data class RunWithAnnotationMeta(val annotationName: String, val import: String) {

    val regex = annotationRegex(annotationName)

    fun extract(line: String): String? {
        val detectedRunWith = regex.find(line, startIndex = 0)?.groupValues?.get(0) ?: return null
        return detectedRunWith
            .split("@$annotationName(")[1]
            .split(")")[0]
    }

    companion object {
        private fun annotationRegex(annotationName: String) = Regex(
            pattern = "@$annotationName\\([^)]*\\)",
            options = setOf(RegexOption.MULTILINE),
        )
    }
}

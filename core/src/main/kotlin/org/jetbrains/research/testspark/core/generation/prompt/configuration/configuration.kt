package org.jetbrains.research.testspark.core.generation.prompt.configuration

data class PromptGenerationContext(
    val cut: ClassRepresentation,
    val classesToTest: List<ClassRepresentation>,
    val polymorphismRelations: Map<ClassRepresentation, List<ClassRepresentation>>,
)

data class ClassRepresentation(
    val qualifiedName: String?,
    val fullText: String,
    val methods: List<MethodRepresentation>,
)

data class MethodRepresentation(
    val signature: String,
    val name: String,
    val text: String,
    val containingClassQualifiedName: String
)

data class PromptTemplates(
    val classPrompt: String,
    val methodPrompt: String,
    val linePrompt: String,
)

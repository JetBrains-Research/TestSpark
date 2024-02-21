package org.jetbrains.research.testspark.core.generation

val importPattern =
    Regex(
        pattern = "^import\\s+(static\\s)?((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
        options = setOf(RegexOption.MULTILINE),
    )

val packagePattern =
    Regex(
        pattern = "^package\\s+((?:[a-zA-Z_]\\w*\\.)*[a-zA-Z_](?:\\w*\\.?)*)(?:\\.\\*)?;",
        options = setOf(RegexOption.MULTILINE),
    )

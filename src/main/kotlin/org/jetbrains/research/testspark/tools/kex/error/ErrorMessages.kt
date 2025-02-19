package org.jetbrains.research.testspark.tools.kex.error

import org.jetbrains.research.testspark.bundles.kex.KexMessagesBundle
import org.jetbrains.research.testspark.core.exception.LlmException

val LlmException.llmDisplayMessage: String
    get() = when (this) {
        is LlmException.MissingGeneratedTests -> KexMessagesBundle.get("testsDontExist")
        is LlmException.IncorrectJavaVersion -> KexMessagesBundle.get("incorrectJavaVersion")
        is LlmException.Common -> KexMessagesBundle.get("kexErrorCommon").format(message)
    }

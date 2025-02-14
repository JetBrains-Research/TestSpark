package org.jetbrains.research.testspark.tools.kex.error

import org.jetbrains.research.testspark.bundles.kex.KexMessagesBundle
import org.jetbrains.research.testspark.core.exception.KexException

val KexException.kexDisplayMessage: String
    get() = when (this) {
        is KexException.MissingGeneratedTests -> KexMessagesBundle.get("testsDontExist")
        is KexException.IncorrectJavaVersion -> KexMessagesBundle.get("incorrectJavaVersion")
        is KexException.Common -> KexMessagesBundle.get("kexErrorCommon").format(message)
    }
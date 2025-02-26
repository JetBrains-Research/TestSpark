package org.jetbrains.research.testspark.tools.error.message

import org.jetbrains.research.testspark.core.exception.CommonException
import org.jetbrains.research.testspark.core.exception.ProcessCancelledException

val CommonException.commonExceptionMessage: String?
    get() = when(this) {
        is ProcessCancelledException -> null
    }

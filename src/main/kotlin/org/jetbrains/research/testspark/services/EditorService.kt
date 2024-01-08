package org.jetbrains.research.testspark.services

import com.intellij.openapi.components.Service
import com.intellij.openapi.editor.Editor

@Service(Service.Level.PROJECT)
class EditorService {
    var editor: Editor? = null
}

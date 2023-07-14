package org.jetbrains.research.testgenie.data

class CodeType {
    enum class Type {
        CLASS, METHOD, LINE
    }

    var objectDescription: String = ""

    var objectIndex: Int = 0
}

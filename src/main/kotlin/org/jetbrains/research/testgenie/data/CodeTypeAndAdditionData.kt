package org.jetbrains.research.testgenie.data

// TODO rename this class
class CodeTypeAndAdditionData {
    var type: CodeType? = null

    var objectDescription: String = ""

    var objectIndex: Int = 0

    constructor(type: CodeType) {
        this.type = type
    }

    constructor(type: CodeType, objectDescription: String) {
        this.type = type
        this.objectDescription = objectDescription
    }

    constructor(type: CodeType, objectIndex: Int) {
        this.type = type
        this.objectIndex = objectIndex
    }
}

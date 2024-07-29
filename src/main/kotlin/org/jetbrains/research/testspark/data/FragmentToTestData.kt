package org.jetbrains.research.testspark.data

import org.jetbrains.research.testspark.core.test.data.CodeType

/**
 * Data about test objects that require test generators.
 */
class FragmentToTestData {

    // Variable which contains all code elements for which it is possible to request test generation.
    var type: CodeType? = null

    // Additional description of the method (grammar https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3).
    var objectDescription: String = ""

    // Additional description of the string (its number).
    var objectIndex: Int = 0

    /**
     * Constructor for a class.
     *
     * @param type contains all code elements for which it is possible to request test generation
     */
    constructor(type: CodeType) {
        assert(type == CodeType.CLASS)
        this.type = type
    }

    /**
     * Constructor for a method.
     *
     * @param type contains all code elements for which it is possible to request test generation
     * @param objectDescription is additional description of the method (grammar https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3).
     */
    constructor(type: CodeType, objectDescription: String) {
        assert(type == CodeType.METHOD)
        this.type = type
        this.objectDescription = objectDescription
    }

    /**
     * Constructor for a line.
     *
     * @param type contains all code elements for which it is possible to request test generation
     * @param objectIndex is additional description of the string (its number).
     */
    constructor(type: CodeType, objectIndex: Int) {
        assert(type == CodeType.LINE)
        this.type = type
        this.objectIndex = objectIndex
    }
}

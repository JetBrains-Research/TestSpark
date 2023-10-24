package org.jetbrains.research.testspark.data

/**
 * Data about test objects that require test generators.
 */
class FragmentToTestData {

    // Variable which contains all code elements for which it is possible to request test generation.
    var type: Level? = null

    // Additional description of the method (grammar https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3).
    var objectDescription: String = ""

    // Additional description of the string (its number).
    var objectIndex: Int = 0

    /**
     * Constructor for a class.
     *
     * @param type contains all code elements for which it is possible to request test generation
     */
    constructor(type: Level) {
        assert(type == Level.CLASS)
        this.type = type
    }

    /**
     * Constructor for a method.
     *
     * @param type contains all code elements for which it is possible to request test generation
     * @param objectDescription is additional description of the method (grammar https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html#jvms-4.3).
     */
    constructor(type: Level, objectDescription: String) {
        assert(type == Level.METHOD)
        this.type = type
        this.objectDescription = objectDescription
    }

    /**
     * Constructor for a line.
     *
     * @param type contains all code elements for which it is possible to request test generation
     * @param objectIndex is additional description of the string (its number).
     */
    constructor(type: Level, objectIndex: Int) {
        assert(type == Level.LINE)
        this.type = type
        this.objectIndex = objectIndex
    }
}

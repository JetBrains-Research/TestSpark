package org.jetbrains.research.testspark.helpers.storage

/**
 * The class is a descriptor of the block in which the value is stored.
 */

class FileBlockLocation(fileName: String, offset: Int, size: Int) {
    var fileName: String
    var offset: Int
    var size: Int

    init {
        this.fileName = fileName
        this.offset = offset
        this.size = size
    }
}


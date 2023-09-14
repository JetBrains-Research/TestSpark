package org.jetbrains.research.testspark.helpers.storage

/**
 * Класс-дескриптор блока, в котором хранится значение.
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


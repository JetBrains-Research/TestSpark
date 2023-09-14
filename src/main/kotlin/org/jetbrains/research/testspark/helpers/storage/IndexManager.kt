package org.jetbrains.research.testspark.helpers.storage

import java.io.Closeable
import java.io.IOException


interface IndexManager : Closeable {
    /**
     * Создает связь key -> listOf(FileBlockLocation) в индексе
     */
    @Throws(IOException::class)
    fun add(key: ByteArray?, writtenBlocks: List<FileBlockLocation?>?)

    @Throws(IOException::class)
    fun remove(key: ByteArray?)

    /**
     * Возвращает список блоков, в которых хранится значение
     */
    @Throws(IOException::class)
    fun getFileBlocksLocations(key: ByteArray?): List<FileBlockLocation?>?
}


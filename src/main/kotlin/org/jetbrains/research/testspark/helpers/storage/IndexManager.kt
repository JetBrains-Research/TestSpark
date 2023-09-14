package org.jetbrains.research.testspark.helpers.storage

import java.io.Closeable
import java.io.IOException


interface IndexManager : Closeable {
    /**
     * Creates a {key -> listOf(FileBlockLocation)} relationship in the index
     */
    @Throws(IOException::class)
    fun add(key: ByteArray?, writtenBlocks: List<FileBlockLocation?>?)

    /**
     * Removes the entry associated with key
     */
    @Throws(IOException::class)
    fun remove(key: ByteArray?)

    /**
     * Returns a list of blocks in which the value is stored
     */
    @Throws(IOException::class)
    fun getFileBlocksLocations(key: ByteArray?): List<FileBlockLocation?>?
}


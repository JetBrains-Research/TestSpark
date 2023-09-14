package org.jetbrains.research.testspark.helpers.storage

import java.io.Closeable
import java.io.IOException
import java.io.InputStream


/**
 * A class that writes/reads values to files
 */
internal interface ValueStoreManager : Closeable {
    /**
     * Writes the value to a file, returns the blocks in which the value was written to add this information to the index
     */
    @Throws(IOException::class)
    fun add(value: ByteArray): List<FileBlockLocation?>?

    /**
     * Returns an input stream from which a value from a specific block can be read
     */
    @Throws(IOException::class)
    fun openBlockStream(location: FileBlockLocation?): InputStream

    /**
     * Adds deleted blocks to the list of free blocks
     */
    @Throws(IOException::class)
    fun remove(valueBlocksLocations: List<FileBlockLocation?>?)
}


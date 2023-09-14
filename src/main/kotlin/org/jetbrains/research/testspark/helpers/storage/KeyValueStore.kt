package org.jetbrains.research.testspark.helpers.storage

import java.io.Closeable
import java.io.IOException
import java.io.InputStream


interface KeyValueStore : Closeable {
    /**
     * Checks whether the provided key exists in the storage
     */
    @Throws(IOException::class)
    operator fun contains(key: ByteArray?): Boolean

    /**
     * Returns by key an input stream from which you can (lazily) read the value
     *
     */
    @Throws(IOException::class)
    fun openValueStream(key: ByteArray?): InputStream?

    /**
     * Completely reads the value into an array of bytes and returns it
     */
    @Throws(IOException::class)
    fun loadValue(key: ByteArray?): ByteArray?

    /**
     * Writes a new value by key. If the key already exists in the storage, then it overwrites the old value
     */
    @Throws(IOException::class)
    fun upsert(key: ByteArray?, value: ByteArray?)

    /**
     * Deletes a value from the storage. If the value existed, it returns true, otherwise false.
     */
    @Throws(IOException::class)
    fun remove(key: ByteArray?): Boolean
}


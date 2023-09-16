package org.jetbrains.research.testspark.helpers.storage

import java.io.IOException
import java.io.InputStream


interface KeyValueStore {
    /**
     * Checks whether the provided key exists in the store
     */
    @Throws(IOException::class)
    operator fun contains(key: String): Boolean

    /**
     * Retrieves value associated with a provided key if exists
     */
    @Throws(IOException::class)
    fun get(key: String): String?

    /**
     * Stores a new value by key. If the key already exists in the store, then it overwrites the old value
     */
    @Throws(IOException::class)
    fun upsert(key: String, value: String)

    /**
     * Deletes a value from the storage. If the value existed, it returns true, otherwise false.
     */
    @Throws(IOException::class)
    fun remove(key: String): Boolean
}


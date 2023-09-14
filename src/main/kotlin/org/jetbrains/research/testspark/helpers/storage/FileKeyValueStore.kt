package org.jetbrains.research.testspark.helpers.storage

import java.io.IOException
import java.io.InputStream
import java.io.SequenceInputStream
import java.nio.file.Path
import java.util.*


class FileKeyValueStore internal constructor(workingDir: Path?, valueFileSize: Int) : KeyValueStore {
    private val indexManager: IndexManager
    private val valueStoreManager: ValueStoreManager
    private var closed = false

    init {
        indexManager = FileIndexManager(workingDir!!)
        valueStoreManager = FileValueStoreManager(workingDir, valueFileSize)
    }

    @Throws(IOException::class)
    override fun contains(key: ByteArray?): Boolean {
        Objects.requireNonNull(key)
        check(!closed) { "Cannot use FileKeyValueStore instance after calling close()" }
        return indexManager.getFileBlocksLocations(key) != null
    }

    @Throws(IOException::class)
    override fun openValueStream(key: ByteArray?): InputStream? {
        Objects.requireNonNull(key)
        check(!closed) { "Cannot use FileKeyValueStore instance after calling close()" }
        val locations = indexManager.getFileBlocksLocations(key)
                ?: throw IOException("Entry with key '" + Arrays.toString(key) + "' not found")
        var concatenatedStream = SequenceInputStream.nullInputStream()
        for (location in locations) {
            concatenatedStream = SequenceInputStream(concatenatedStream, valueStoreManager.openBlockStream(location!!))
        }
        return concatenatedStream
    }

    @Throws(IOException::class)
    override fun loadValue(key: ByteArray?): ByteArray? {
        Objects.requireNonNull(key)
        check(!closed) { "Cannot use FileKeyValueStore instance after calling close()" }
        val locations = indexManager.getFileBlocksLocations(key)
                ?: throw IOException("Entry with key '" + Arrays.toString(key) + "' not found")
        val totalSize: Int = locations.sumOf { it!!.size }
        val buffer = ByteArray(totalSize)
        openValueStream(key).use { input -> input!!.readNBytes(buffer, 0, buffer.size) }
        return buffer
    }

    @Throws(IOException::class)
    override fun upsert(key: ByteArray?, value: ByteArray?) {
        Objects.requireNonNull(key)
        Objects.requireNonNull(value)
        check(!closed) { "Cannot use FileKeyValueStore instance after calling close()" }
        val occupiedBlocks = valueStoreManager.add(value!!)
        indexManager.add(key, occupiedBlocks)
    }

    @Throws(IOException::class)
    override fun remove(key: ByteArray?): Boolean {
        Objects.requireNonNull(key)
        check(!closed) { "Cannot use FileKeyValueStore instance after calling close()" }
        val occupiedLocations = indexManager.getFileBlocksLocations(key)
        if (occupiedLocations != null) {
            indexManager.remove(key)
            valueStoreManager.remove(occupiedLocations)
        }
        return occupiedLocations != null
    }

    @Throws(IOException::class)
    override fun close() {
        valueStoreManager.close()
        indexManager.close()
        closed = true
    }
}


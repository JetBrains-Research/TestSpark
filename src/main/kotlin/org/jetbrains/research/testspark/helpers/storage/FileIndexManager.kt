package org.jetbrains.research.testspark.helpers.storage

import java.io.*
import java.nio.file.Files
import java.nio.file.Path


/**
 * Format of index file:
 * [number of entries]
 * Entry:
 * [size of key (aka number of bytes)]
 * [key]
 * [number of `FileBlockLocation` instances associated with key]
 * FileBlockLocation:
 * [fileName]
 * [offset]
 * [size]
 */
class FileIndexManager internal constructor(private val workingDir: Path) : IndexManager {
    private val indexFilepath: Path = Path.of(workingDir.toString(), INDEX_FILENAME)
    private val locations: MutableMap<ByteWrapper, List<FileBlockLocation?>?> = HashMap()

    init {
        if (!Files.exists(indexFilepath)) {
            Files.createFile(indexFilepath)
        } else {
            // read data from index file and store it in locations
            loadStateFromIndex()
        }
    }

    @Throws(IOException::class)
    private fun loadStateFromIndex() {
        DataInputStream(FileInputStream(indexFilepath.toFile())).use { input ->
            val entriesCount: Int = input.readInt()
            for (i in 0 until entriesCount) {
                val key: ByteArray = readKeyBytes(input, i)
                val blocks: List<FileBlockLocation?> = readFileBlockLocations(input)
                locations[ByteWrapper(key)] = blocks
            }
        }
    }

    @Throws(IOException::class)
    private fun readKeyBytes(input: DataInputStream, entryNumber: Int): ByteArray {
        val keyBytesCount = input.readInt()
        val key = input.readNBytes(keyBytesCount)
        if (key.size != keyBytesCount) {
            throw RuntimeException(
                    "Inconsistent '" + indexFilepath.toString() +
                            "' state: reading " + entryNumber + "th entry, expected key length " +
                            keyBytesCount + ", got " + key.size)
        }
        return key
    }

    @Throws(IOException::class)
    private fun readFileBlockLocations(input: DataInputStream): List<FileBlockLocation?> {
        val fileBlockLocationsCount = input.readInt()
        val blocks: MutableList<FileBlockLocation?> = ArrayList(fileBlockLocationsCount)
        for (i in 0 until fileBlockLocationsCount) {
            val block = readSingleFileBlockLocation(input)
            blocks.add(block)
        }
        return blocks
    }

    @Throws(IOException::class)
    private fun readSingleFileBlockLocation(input: DataInputStream): FileBlockLocation {
        val fileName = input.readUTF()
        val offset = input.readInt()
        val size = input.readInt()
        return FileBlockLocation(fileName, offset, size)
    }

    @Throws(IOException::class)
    override fun add(key: ByteArray?, writtenBlocks: List<FileBlockLocation?>?) {
        locations[ByteWrapper((key)!!)] = writtenBlocks
    }

    @Throws(IOException::class)
    override fun remove(key: ByteArray?) {
        locations.remove(ByteWrapper((key)!!))
    }

    @Throws(IOException::class)
    override fun getFileBlocksLocations(key: ByteArray?): List<FileBlockLocation?>? {
        return locations[ByteWrapper((key)!!)]
    }

    @Throws(IOException::class)
    override fun close() {
        storeStateInIndex()
    }

    @Throws(IOException::class)
    private fun storeStateInIndex() {
        DataOutputStream(FileOutputStream(indexFilepath.toFile())).use { output ->
            // store number of entries
            val entriesCount: Int = locations.size
            output.writeInt(entriesCount)

            // store entries
            for (entry: Map.Entry<ByteWrapper, List<FileBlockLocation?>?> in locations.entries) {
                val key: ByteArray = entry.key.bytes
                val blocks: List<FileBlockLocation?>? = entry.value

                // store key size
                output.writeInt(key.size)
                // store key bytes
                output.write(key)

                // store number of blocks
                output.writeInt(blocks!!.size)

                // store every block
                for (block: FileBlockLocation? in blocks) {
                    writeSingleFileBlockLocation(output, block)
                }
            }
        }
    }

    @Throws(IOException::class)
    private fun writeSingleFileBlockLocation(output: DataOutputStream, block: FileBlockLocation?) {
        output.writeUTF(block!!.fileName)
        output.writeInt(block.offset)
        output.writeInt(block.size)
    }

    companion object {
        private const val INDEX_FILENAME = "index.txt"
    }
}


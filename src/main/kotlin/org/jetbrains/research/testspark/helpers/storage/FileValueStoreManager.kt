package org.jetbrains.research.testspark.helpers.storage

import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.*


/**
 * Format of index file with free file block locations:
 *      [number of free file block locations]
 *      FileBlockLocation:
 *          [fileName]
 *          [offset]
 *          [size]
 */
class FileValueStoreManager internal constructor(private val workingDir: Path, private val valueFileSize: Int) : ValueStoreManager {
    private val storeDirpath: Path = Path.of(workingDir.toString(), STORE_DIRNAME)
    private val indexFilepath: Path = Path.of(workingDir.toString(), FREE_BLOCKS_INDEX_FILENAME)
    private val freeLocations: MutableList<FileBlockLocation> = ArrayList()

    init {
        if (!Files.exists(storeDirpath)) {
            Files.createDirectory(storeDirpath)
            Files.createFile(indexFilepath)
        } else {
            loadFreeLocationsFromIndex()
        }
    }

    @Throws(IOException::class)
    private fun loadFreeLocationsFromIndex() {
        DataInputStream(FileInputStream(indexFilepath.toFile())).use { input ->
            val freeLocationsCount = input.readInt()
            for (i in 0 until freeLocationsCount) {
                val fileName = input.readUTF()
                val offset = input.readInt()
                val size = input.readInt()
                val block = FileBlockLocation(fileName, offset, size)
                freeLocations.add(block)
            }
        }
    }

    @Throws(IOException::class)
    fun demandNewFreeLocation(requiredBytesCount: Int): FileBlockLocation {
        if (freeLocations.isEmpty()) {
            createNewFreeLocation()
        }
        assert(freeLocations.isNotEmpty())
        // retrieving last location
        val location = freeLocations.removeAt(freeLocations.size - 1)
        if (location.size <= requiredBytesCount) {
            return location
        }
        /**
         * Block splitting algorithm:
         * if size of location is greater than the required count then split the block into 2 pieces.
         * b := requiredBytesCount; s := location.size()
         * [0..b..s) -> [0..b) + [b..s)
         * Then mark the latter piece as free location and return the former piece.
         */
        val unoccupiedLocation = FileBlockLocation(
                location.fileName,
                location.offset + requiredBytesCount,
                location.size - requiredBytesCount)
        freeLocations.add(unoccupiedLocation)
        return FileBlockLocation(location.fileName, location.offset, requiredBytesCount)
    }

    @Throws(IOException::class)
    private fun createNewFreeLocation() {
        // creating new file and save it as free file block location
        val filename = UUID.randomUUID().toString()
        val filepath = Path.of(storeDirpath.toString(), filename)
        Files.createFile(filepath)
        val location = FileBlockLocation(filename, 0, valueFileSize)
        freeLocations.add(location)
    }

    @Throws(IOException::class)
    override fun add(value: ByteArray): List<FileBlockLocation> {
        val occupiedLocations: MutableList<FileBlockLocation> = ArrayList()

        // number of bytes already written into files
        var offset = 0
        while (offset < value.size) {
            val bytesLeft = value.size - offset
            val location = demandNewFreeLocation(bytesLeft)
            assert(value.size - offset >= location.size)
            partiallyWriteValueIntoLocation(value, offset, location)
            offset += location.size
            occupiedLocations.add(location)
        }
        return occupiedLocations
    }

    @Throws(IOException::class)
    private fun partiallyWriteValueIntoLocation(value: ByteArray, offset: Int, location: FileBlockLocation) {
        val filepath = Path.of(storeDirpath.toString(), location.fileName)
        RandomAccessFile(filepath.toFile(), "rw").use { output ->
            output.seek(location.offset.toLong())
            assert(value.size - offset >= location.size)
            output.write(value, offset, location.size)
        }
    }

    @Throws(IOException::class)
    override fun openBlockStream(location: FileBlockLocation?): InputStream {
        val filepath = Path.of(storeDirpath.toString(), location!!.fileName)
        val buffer = ByteArray(location.size)
        FileInputStream(filepath.toFile()).use { input ->
            // skipping location.offset() bytes of the input stream
            run {
                val bytesSkipped = input.skip(location.offset.toLong())
                if (bytesSkipped.toInt() != location.offset) {
                    throw RuntimeException(
                            "Reading data from '" + location.fileName +
                                    "' with offset " + location.offset + " and size " + location.size +
                                    ": number of bytes skipped " + bytesSkipped + ", expected " + location.offset)
                }
            }

            // reading buffer.length bytes into buffer
            run {
                val bytesRead = input.readNBytes(buffer, 0, buffer.size)
                if (bytesRead != location.size) {
                    throw RuntimeException(
                            "Reading data from '" + location.fileName +
                                    "' with offset " + location.offset + " and size " + location.size +
                                    ": number of bytes read " + bytesRead + ", expected " + buffer.size)
                }
            }
        }
        return ByteArrayInputStream(buffer)
    }

    @Throws(IOException::class)
    override fun remove(valueBlocksLocations: List<FileBlockLocation?>?) {
        freeLocations.addAll(valueBlocksLocations!!.filterNotNull())
    }

    @Throws(IOException::class)
    override fun close() {
        DataOutputStream(FileOutputStream(indexFilepath.toFile())).use { output ->
            // store number of free locations
            output.writeInt(freeLocations.size)

            // store every free location
            for (location in freeLocations) {
                output.writeUTF(location.fileName)
                output.writeInt(location.offset)
                output.writeInt(location.size)
            }
        }
    }

    companion object {
        private const val STORE_DIRNAME = "store"
        private const val FREE_BLOCKS_INDEX_FILENAME = "store.txt"
    }
}


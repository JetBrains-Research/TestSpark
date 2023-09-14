package org.jetbrains.research.testspark.helpers.storage

import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.NotDirectoryException
import java.nio.file.Path


object KeyValueStoreFactory {
    @Throws(IOException::class)
    fun create(workingDir: Path?, valueFileSize: Int = 1024): KeyValueStore {
        requireNotNull(workingDir) { "Working directory must not be null" }
        require(valueFileSize > 0) { "Value file size must be positive, got $valueFileSize" }
        if (!Files.exists(workingDir)) {
            throw NoSuchFileException("Working directory $workingDir does not exist")
        }
        if (!Files.isDirectory(workingDir)) {
            throw NotDirectoryException("Provided path '$workingDir' does not denote to a directory")
        }
        return FileKeyValueStore(workingDir, valueFileSize)
    }
}


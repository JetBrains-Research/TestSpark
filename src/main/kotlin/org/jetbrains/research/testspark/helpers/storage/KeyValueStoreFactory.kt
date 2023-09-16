package org.jetbrains.research.testspark.helpers.storage

import java.io.IOException
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.NotDirectoryException
import java.nio.file.Path


object KeyValueStoreFactory {
    @Throws(IOException::class)
    fun create(filepath: Path): KeyValueStore {
        if (!Files.exists(filepath) || Files.isDirectory(filepath)) {
            throw NoSuchFileException("File '$filepath' does not exist")
        }
        return JsonKeyValueStore(filepath)
    }
}


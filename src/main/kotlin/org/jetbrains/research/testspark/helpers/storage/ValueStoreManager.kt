package org.jetbrains.research.testspark.helpers.storage

import java.io.Closeable
import java.io.IOException
import java.io.InputStream


/**
 * Класс, который занимается записью/чтением значений в файлы
 */
internal interface ValueStoreManager : Closeable {
    /**
     * Записывает значение в файл, возвращает блоки, в которые было записано значение для добавления
     * этой информации в индекс
     */
    @Throws(IOException::class)
    fun add(value: ByteArray): List<FileBlockLocation?>?

    /**
     * Возвращает входной поток из которого можно читать значение из конкретного блока
     */
    @Throws(IOException::class)
    fun openBlockStream(location: FileBlockLocation?): InputStream

    /**
     * Добавляет удаленные блоки в список свободных блоков
     */
    @Throws(IOException::class)
    fun remove(valueBlocksLocations: List<FileBlockLocation?>?)
}


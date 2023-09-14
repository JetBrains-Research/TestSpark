package org.jetbrains.research.testspark.helpers.storage

import java.io.Closeable
import java.io.IOException
import java.io.InputStream


interface KeyValueStore : Closeable {
    /**
     * Проверяет, есть ли такой ключ в хранилище
     */
    @Throws(IOException::class)
    operator fun contains(key: ByteArray?): Boolean

    /**
     * По ключу возвращает входной поток из которого можно (лениво) читать значение
     */
    @Throws(IOException::class)
    fun openValueStream(key: ByteArray?): InputStream?

    /**
     * Полностью считывает значение в массив байтов и возвращает его
     */
    @Throws(IOException::class)
    fun loadValue(key: ByteArray?): ByteArray?

    /**
     * Записывает новое значение по ключу. Если ключ уже существует в базе, тогда перезаписывает
     * старое значение
     */
    @Throws(IOException::class)
    fun upsert(key: ByteArray?, value: ByteArray?)

    /**
     * Удаляет значение из базы. Если значение существовало, то возвращает true, иначе false.
     */
    @Throws(IOException::class)
    fun remove(key: ByteArray?): Boolean

    /**
     * TestOnly
     *
     *
     * Возвращает IndexManager, соответствующий текущему хранилищу
     */
    val indexManager: IndexManager?
}


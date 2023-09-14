package org.jetbrains.research.testspark.helpers.storage


/**
 * Вспомогательная обертка над массивом байтов, понадобится для хранения Map<ByteWrapper></ByteWrapper>,
 * List<FileBlockLocation>> в [IndexManager] </FileBlockLocation>
 */
internal class ByteWrapper(val bytes: ByteArray) {

    override fun equals(o: Any?): Boolean {
        return if (this === o) {
            true
        } else o is ByteWrapper && bytes.contentEquals(o.bytes)
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}
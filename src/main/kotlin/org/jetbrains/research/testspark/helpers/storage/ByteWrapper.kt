package org.jetbrains.research.testspark.helpers.storage


/**
 * Auxiliary wrapper over the byte array used for proper storing inside Map<ByteWrapper>
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
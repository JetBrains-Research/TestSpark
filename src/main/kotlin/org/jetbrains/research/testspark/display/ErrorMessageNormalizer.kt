package org.jetbrains.research.testspark.display

object ErrorMessageNormalizer {
    /**
     * Normalizes an error message by inserting "<br/>" tags after every 300 characters,
     * except if there is already a "<br/>" tag within the blockSize characters.
     * If the string length is a multiple of blockSize and the last character is a "<br/>" tag,
     * it is removed from the result.
     *
     * @param error The error message to be normalized.
     * @return The normalized error message.
     */
    fun normalize(error: String): String {
        // set default parameters
        val blockSize = 100
        val separator = "<br/>"

        // init variables
        val builder = StringBuilder()
        var lastIndex = 0

        // string separating
        while (lastIndex < error.length) {
            val nextIndex = (lastIndex + blockSize).coerceAtMost(error.length)
            val substring = error.substring(lastIndex, nextIndex)

            if (!substring.contains(separator)) {
                builder.append(substring).append(separator)
            } else {
                builder.append(substring)
            }

            lastIndex = nextIndex
        }

        // remove the last <br/> if the string length is a multiple of 300 and it didn't have <br/>
        if (builder.endsWith(separator) && (error.length % blockSize == 0)) {
            builder.setLength(builder.length - separator.length)
        }

        return builder.toString()
    }
}

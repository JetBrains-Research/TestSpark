package org.jetbrains.research.testspark.display.utils

object ModifiedLinesGetter {
    /**
     * Returns the indexes of lines that are modified between two lists of strings.
     *
     * @param source The source list of strings.
     * @param target The target list of strings.
     * @return The indexes of modified lines.
     */
    fun getLines(source: List<String>, target: List<String>): List<Int> {
        val dp = Array(source.size + 1) { IntArray(target.size + 1) }

        for (i in 1..source.size) {
            for (j in 1..target.size) {
                if (source[i - 1] == target[j - 1]) {
                    dp[i][j] = dp[i - 1][j - 1] + 1
                } else {
                    dp[i][j] = maxOf(dp[i - 1][j], dp[i][j - 1])
                }
            }
        }

        var i = source.size
        var j = target.size

        val modifiedLineIndexes = mutableListOf<Int>()

        while (i > 0 && j > 0) {
            if (source[i - 1] == target[j - 1]) {
                i--
                j--
            } else if (dp[i][j] == dp[i - 1][j]) {
                i--
            } else if (dp[i][j] == dp[i][j - 1]) {
                modifiedLineIndexes.add(j - 1)
                j--
            }
        }

        while (j > 0) {
            modifiedLineIndexes.add(j - 1)
            j--
        }

        modifiedLineIndexes.reverse()

        return modifiedLineIndexes
    }
}

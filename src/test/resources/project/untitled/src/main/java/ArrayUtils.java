
/**
 * (From Apache Commons Lang: https://github.com/apache/commons-lang/blob/d2687419c6973572d1621afc1b8546f5262769c3/src/main/java/org/apache/commons/lang3/ArrayUtils.java#L2325)
 */
class ArrayUtils {

    /**
     * <code>ArrayUtils</code> should not normally be instantiated.
     */
    private ArrayUtils() {
    }

    /**
     * The index value when an element is not found in a list or array: {@code -1}.
     * This value is returned by methods in this class and can also be used in
     * comparisons with values returned by various method from
     * {@link java.util.List}.
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * Finds the index of the given value in the array starting at the given index.
     *
     * <p>
     * This method returns {@link #INDEX_NOT_FOUND} ({@code -1}) for a {@code null}
     * input array.
     *
     * <p>
     * A negative startIndex is treated as zero. A startIndex larger than the array
     * length will return {@link #INDEX_NOT_FOUND} ({@code -1}).
     *
     * @param array       the array to search through for the object, may be {@code null}
     * @param valueToFind the value to find
     * @param startIndex  the index to start searching at
     * @return the index of the value within the array, {@link #INDEX_NOT_FOUND}
     * ({@code -1}) if not found or {@code null} array input
     */
    public static int indexOf(final int[] array, final int valueToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        for (int i = startIndex; i < array.length; i++) {
            if (valueToFind == array[i]) {
                return i;
            }
        }
        return INDEX_NOT_FOUND;
    }
}
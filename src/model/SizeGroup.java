package model;

/**
 * Maps a party size to the queue it waits in.
 *
 * @author Sathvik Verma
 * @version 2026.11.11
 */
public enum SizeGroup {
    /** Parties of 1-2. */
    SMALL(1, 2),
    /** Parties of 3-4. */
    MEDIUM(3, 4),
    /** Parties of 5-6. */
    LARGE(5, 6);

    private final int minSize;
    private final int maxSize;

    SizeGroup(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    /**
     * @param size a party size
     * @return the group for that size
     * @throws IllegalArgumentException outside 1 to 6
     */
    public static SizeGroup forSize(int size) {
        for (SizeGroup g : values()) {
            if (size >= g.minSize && size <= g.maxSize) {
                return g;
            }
        }
        throw new IllegalArgumentException("No size group for " + size);
    }

    /**
     * @param capacity a table's seat count
     * @return true if every party in this group fits that table
     */
    public boolean fitsAt(int capacity) {
        return maxSize <= capacity;
    }
}

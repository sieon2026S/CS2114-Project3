package model;

/**
 * One walk-in group: its name, size and arrival number. Immutable.
 *
 * @author Zolo (validation fixes by Sathvik Verma)
 * @version 2026.11.11
 */
public class Party {
    /** Largest party: two combined 4-tops seat 8. */
    public static final int MAX_SIZE = 8;
    /** Longest name allowed. */
    public static final int MAX_NAME = 30;

    private final String name;
    private final int size;
    private final int arrivalNumber;

    /**
     * @param name the party name (letters, spaces, hyphens, apostrophes)
     * @param size number of people, 1 to 8
     * @param arrivalNumber order of arrival (lower = earlier)
     * @throws IllegalArgumentException on a blank or invalid name or a size
     *             outside 1 to 8
     */
    public Party(String name, int size, int arrivalNumber) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a name.");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME
            || !trimmed.matches("[A-Za-z][A-Za-z '\\-]*")) {
            throw new IllegalArgumentException(
                "Names can only use letters, spaces, hyphens and "
                    + "apostrophes (30 max).");
        }
        if (size <= 0 || size > MAX_SIZE) {
            throw new IllegalArgumentException(
                "Party size must be a whole number from 1 to 8.");
        }
        if (arrivalNumber < 0) {
            throw new IllegalArgumentException(
                "Arrival number cannot be negative.");
        }
        this.name = trimmed;
        this.size = size;
        this.arrivalNumber = arrivalNumber;
    }

    /** @return the trimmed name */
    public String getName() {
        return name;
    }

    /** @return the number of people */
    public int getSize() {
        return size;
    }

    /** @return the arrival number */
    public int getArrivalNumber() {
        return arrivalNumber;
    }

    /** @return the size group this party waits in */
    public SizeGroup getSizeGroup() {
        return SizeGroup.forSize(size);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Party)) {
            return false;
        }
        return this.name.equalsIgnoreCase(((Party)obj).name);
    }

    @Override
    public int hashCode() {
        return name.toLowerCase().hashCode();
    }
}

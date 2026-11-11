package model;

/**
 * One physical table: its number, seat count and who is sitting there.
 * Only classes in the model package can seat or clear it.
 *
 * @author Sathvik Verma
 * @version 2026.11.11
 */
public class Table {
    private final int number;
    private final int capacity;
    private Party seatedParty;

    /**
     * @param number table number (1 or more)
     * @param capacity seats (2, 4 or 6)
     */
    public Table(int number, int capacity) {
        if (number < 1 || (capacity != 2 && capacity != 4 && capacity != 6)) {
            throw new IllegalArgumentException("Bad table " + number + "/"
                + capacity);
        }
        this.number = number;
        this.capacity = capacity;
    }

    /** @return the table number */
    public int getNumber() {
        return number;
    }

    /** @return the seat count */
    public int getCapacity() {
        return capacity;
    }

    /** @return true if nobody is seated */
    public boolean isFree() {
        return seatedParty == null;
    }

    /** @return who is sitting here, or null if free */
    public Party getSeatedParty() {
        return seatedParty;
    }

    /**
     * Seats a party here.
     *
     * @param party who to seat
     * @throws IllegalStateException if the table is taken
     */
    void seat(Party party) {
        if (party == null) {
            throw new IllegalArgumentException("Party must not be null.");
        }
        if (!isFree()) {
            throw new IllegalStateException("Table " + number
                + " is already taken.");
        }
        seatedParty = party;
    }

    /**
     * Frees the table.
     *
     * @return who left, or null if it was already free
     */
    Party clear() {
        Party leaving = seatedParty;
        seatedParty = null;
        return leaving;
    }
}

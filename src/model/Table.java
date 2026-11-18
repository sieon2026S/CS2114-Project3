package model;

/**
 * One physical table: its number, seat count and who is sitting there.
 * Only classes in the model package can seat or clear it.
 *
 * @author Sathvik Verma
 * @version 2026.11.18
 */
public class Table {
    private final int number;
    private final int capacity;
    private Party seatedParty;
    private Table joinedWith;

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

    /** @return true if this table is pushed together with another one */
    public boolean isJoined() {
        return joinedWith != null;
    }

    /** @return the table this one is joined with, or null */
    public Table getJoinedWith() {
        return joinedWith;
    }

    /**
     * Pushes this table together with another one (both sides are linked).
     *
     * @param other the table to join
     * @throws IllegalArgumentException if other is null or this table
     */
    void joinWith(Table other) {
        if (other == null || other == this) {
            throw new IllegalArgumentException(
                "A table can only be joined with a different table.");
        }
        joinedWith = other;
        other.joinedWith = this;
    }

    /**
     * Splits this table from the one it was joined with (both sides).
     */
    void unjoin() {
        if (joinedWith != null) {
            joinedWith.joinedWith = null;
            joinedWith = null;
        }
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

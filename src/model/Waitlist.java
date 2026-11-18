package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Owns the three size-group queues and decides which waiting party a table
 * of a given size gets. Seating only ever looks at queue fronts.
 *
 * @author Sathvik Verma
 * @version 2026.11.18
 */
public class Waitlist {
    private final LinkedQueue<Party> small = new LinkedQueue<>();
    private final LinkedQueue<Party> medium = new LinkedQueue<>();
    private final LinkedQueue<Party> large = new LinkedQueue<>();
    private final LinkedQueue<Party> xlarge = new LinkedQueue<>();

    private LinkedQueue<Party> queueFor(SizeGroup group) {
        switch (group) {
            case SMALL:
                return small;
            case MEDIUM:
                return medium;
            case LARGE:
                return large;
            default:
                return xlarge;
        }
    }

    /**
     * Adds a party to the back of its size group's queue.
     *
     * @param party the party to add
     */
    public void add(Party party) {
        if (party == null) {
            throw new IllegalArgumentException("Party must not be null.");
        }
        queueFor(party.getSizeGroup()).enqueue(party);
    }

    /**
     * Removes and returns who a table of this capacity should seat: the
     * front of the matching group first; otherwise the earliest-arriving
     * front among smaller groups. Capacity 8 means two combined 4-tops,
     * which only ever seat a party of 7-8 (joining two tables for a smaller
     * party would waste them).
     *
     * @param capacity the table's seat count (2, 4, 6, or 8 when combined)
     * @return the party to seat, or null if nobody fits
     */
    public Party nextFor(int capacity) {
        if (capacity >= 8) {
            return xlarge.isEmpty() ? null : xlarge.dequeue();
        }
        SizeGroup match = capacity >= 6 ? SizeGroup.LARGE
            : capacity >= 4 ? SizeGroup.MEDIUM : SizeGroup.SMALL;
        if (!queueFor(match).isEmpty()) {
            return queueFor(match).dequeue();
        }
        LinkedQueue<Party> best = null;
        for (SizeGroup g : SizeGroup.values()) {
            if (g == match) {
                break;
            }
            LinkedQueue<Party> q = queueFor(g);
            if (!q.isEmpty() && (best == null || q.peek()
                .getArrivalNumber() < best.peek().getArrivalNumber())) {
                best = q;
            }
        }
        return best == null ? null : best.dequeue();
    }

    /**
     * @param party the party to remove
     * @return true if it was waiting and is now removed
     */
    public boolean remove(Party party) {
        return party != null && queueFor(party.getSizeGroup()).remove(party);
    }

    /**
     * @param name a party name (case and outer spaces ignored)
     * @return the waiting party with that name, or null
     */
    public Party find(String name) {
        if (name == null) {
            return null;
        }
        for (Party p : getWaitingInOrder()) {
            if (p.getName().equalsIgnoreCase(name.trim())) {
                return p;
            }
        }
        return null;
    }

    /**
     * @return all waiting parties merged from the three queues, in arrival
     *         order
     */
    public List<Party> getWaitingInOrder() {
        List<Party> all = new ArrayList<>();
        all.addAll(small.toList());
        all.addAll(medium.toList());
        all.addAll(large.toList());
        all.addAll(xlarge.toList());
        Collections.sort(all, new Comparator<Party>() {
            public int compare(Party a, Party b) {
                return a.getArrivalNumber() - b.getArrivalNumber();
            }
        });
        return all;
    }

    /** @return total waiting parties */
    public int size() {
        return small.size() + medium.size() + large.size() + xlarge.size();
    }
    
    /**
     * Estimates the wait time for parties in minutes
     */
    
    /**
     * Estimates the wait time in minutes for a specific waiting party.
     * Assumes an average table turnover of 15 minutes per seating cycle,
     * adjusted for the number of tables available for that party's size group.
     *
     * @param party the waiting party
     * @return estimated wait time in minutes, or -1 if party is null or not waiting
     */
    public int estimateWaitMinutes(Party party) {
        if (party == null) {
            return -1;
        }
        LinkedQueue<Party> queue = queueFor(party.getSizeGroup());
        java.util.List<Party> list = queue.toList();
        int position = list.indexOf(party);
        if (position == -1) {
            return -1;
        }

        int tablesForGroup = tableCountFor(party.getSizeGroup());
        int turnMinutes = 15;
        // Position 0 is next up (1 cycle if tables are currently full)
        return ((position / tablesForGroup) + 1) * turnMinutes;
    }

    /**
     * Estimates the wait time in minutes for a new party of a given size
     * if they were added to the waitlist right now.
     *
     * @param size party size (1 to 8)
     * @return estimated wait time in minutes
     */
    public int estimateWaitForSize(int size) {
        SizeGroup group = sizeGroupFor(size);
        int ahead = queueFor(group).size();
        int tablesForGroup = tableCountFor(group);
        int turnMinutes = 15;
        return ((ahead / tablesForGroup) + 1) * turnMinutes;
    }

    private SizeGroup sizeGroupFor(int size) {
        if (size <= 2) {
            return SizeGroup.SMALL;
        } else if (size <= 4) {
            return SizeGroup.MEDIUM;
        } else if (size <= 6) {
            return SizeGroup.LARGE;
        } else {
            return SizeGroup.XLARGE;
        }
    }

    private int tableCountFor(SizeGroup group) {
        switch (group) {
            case SMALL:
                return 4; // Tables 1-4
            case MEDIUM:
                return 3; // Tables 5-7
            case LARGE:
                return 1; // Table 8
            default:
                return 1; // Combined 4-tops (tables 5-7)
        }
    }

}

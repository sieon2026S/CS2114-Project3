package model;

import java.util.List;

/**
 * Tests for Restaurant, the model facade. Every test builds the model
 * directly with no window or controller, which shows the model works on its
 * own. A CountingListener stands in for the GUI: real changes must send one
 * notification and rejected ones must send none.
 *
 * Fresh restaurant for each test: tables 1-4 seat 2, 5-7 seat 4, 8 seats 6,
 * and the waitlist is empty.
 *
 * @author Sathvik Verma
 * @version 2026.11.11
 */
public class RestaurantTest extends student.TestCase
{

    /**
     * Stand-in for the view: counts modelChanged() calls.
     */
    private static class CountingListener implements RestaurantListener
    {
        private int count = 0;

        @Override
        public void modelChanged()
        {
            count++;
        }

        public int getCount()
        {
            return count;
        }
    }

    private Restaurant restaurant;
    private CountingListener listener;

    /**
     * Runs before every test: a fresh restaurant with a counting listener.
     */
    public void setUp()
    {
        restaurant = new Restaurant();
        listener = new CountingListener();
        restaurant.addListener(listener);
    }

    // ----- helpers ----------------------------------------------------------

    /**
     * Names of the waiting parties, in the order the model returns them.
     */
    private String waitingNames()
    {
        StringBuilder names = new StringBuilder();
        for (Party p : restaurant.getWaitingParties())
        {
            if (names.length() > 0)
            {
                names.append(",");
            }
            names.append(p.getName());
        }
        return names.toString();
    }

    /**
     * The table with the given number (tables are numbered 1-8 in order).
     */
    private Table table(int number)
    {
        return restaurant.getTables().get(number - 1);
    }

    /**
     * Calls addParty and returns the exception it threw, or null.
     */
    private Exception tryAdd(String name, int size)
    {
        try
        {
            restaurant.addParty(name, size);
        }
        catch (Exception e)
        {
            return e;
        }
        return null;
    }

    // ----- starting state ---------------------------------------------------

    /**
     * Normal: a new restaurant has 8 free tables (four 2-tops, three 4-tops,
     * one 6-top) and nobody waiting.
     */
    public void testStartingFloor()
    {
        List<Table> tables = restaurant.getTables();
        assertEquals(8, tables.size());
        int[] capacities = { 2, 2, 2, 2, 4, 4, 4, 6 };
        for (int i = 0; i < 8; i++)
        {
            assertEquals(capacities[i], tables.get(i).getCapacity());
            assertTrue(tables.get(i).isFree());
        }
        assertEquals(0, restaurant.getWaitingParties().size());
    }

    // ----- addParty ---------------------------------------------------------

    /**
     * Normal: add Smith (2), Smith is on the waitlist and the view is told
     * once.
     */
    public void testAddParty()
    {
        restaurant.addParty("Smith", 2);
        assertEquals("Smith", waitingNames());
        assertEquals(1, listener.getCount());
    }

    /**
     * Bad input: size 0, -3, 9 and 1000000000 throw, nothing is added, and
     * no notification is sent.
     */
    public void testAddPartyBadSizes()
    {
        int[] badSizes = { 0, -3, 9, 1000000000 };
        for (int size : badSizes)
        {
            Exception thrown = tryAdd("Smith", size);
            assertNotNull("size " + size + " should be rejected", thrown);
            assertTrue(thrown instanceof IllegalArgumentException);
        }
        assertEquals(0, restaurant.getWaitingParties().size());
        assertEquals(0, listener.getCount());
    }

    /**
     * Bad input: blank, empty and invalid-character names throw and nothing
     * is added.
     */
    public void testAddPartyBadNames()
    {
        String[] badNames = { "", "   ", "@d@m", "Z010" };
        for (String name : badNames)
        {
            Exception thrown = tryAdd(name, 2);
            assertNotNull("name \"" + name + "\" should be rejected", thrown);
            assertTrue(thrown instanceof IllegalArgumentException);
        }
        assertEquals(0, restaurant.getWaitingParties().size());
        assertEquals(0, listener.getCount());
    }

    /**
     * Bad input: a duplicate name differing only in case and spaces is also
     * refused, and so is the name of a party already seated.
     */
    public void testAddPartyDuplicateIgnoresCaseAndSeated()
    {
        restaurant.addParty("Smith", 2);
        assertTrue(tryAdd("  smith ", 2) instanceof IllegalArgumentException);
        restaurant.seatAt(1);
        assertTrue(tryAdd("SMITH", 2) instanceof IllegalArgumentException);
        assertEquals(0, restaurant.getWaitingParties().size());
    }


    /**
     * Bad input: a duplicate name is refused and the list keeps one Smith.
     */
    public void testAddPartyDuplicateName()
    {
        restaurant.addParty("Smith", 2);
        Exception thrown = tryAdd("Smith", 4);
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalArgumentException);
        assertEquals("Smith", waitingNames());
        assertEquals(1, listener.getCount());
    }

    // ----- seatAt -----------------------------------------------------------

    /**
     * Normal: seat Smith at table 1, table 1 is taken by Smith and Smith is
     * off the waitlist.
     */
    public void testSeatAt()
    {
        restaurant.addParty("Smith", 2);
        restaurant.seatAt(1);
        assertFalse(table(1).isFree());
        assertEquals("Smith", table(1).getSeatedParty().getName());
        assertEquals(0, restaurant.getWaitingParties().size());
        assertEquals(2, listener.getCount());
    }

    /**
     * Bad input: seating at an occupied table throws and changes nothing.
     */
    public void testSeatAtOccupiedTable()
    {
        restaurant.addParty("Smith", 2);
        restaurant.addParty("Lee", 2);
        restaurant.seatAt(1);
        int before = listener.getCount();
        Exception thrown = null;
        try
        {
            restaurant.seatAt(1);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertEquals("Smith", table(1).getSeatedParty().getName());
        assertEquals("Lee", waitingNames());
        assertEquals(before, listener.getCount());
    }

    /**
     * Bad input: table 99 (and 0) does not exist, so seatAt throws.
     */
    public void testSeatAtMissingTable()
    {
        restaurant.addParty("Smith", 2);
        int[] badIds = { 99, 0, -1 };
        for (int id : badIds)
        {
            Exception thrown = null;
            try
            {
                restaurant.seatAt(id);
            }
            catch (Exception e)
            {
                thrown = e;
            }
            assertNotNull("table " + id + " should be rejected", thrown);
        }
        assertEquals("Smith", waitingNames());
    }

    // ----- removeParty ------------------------------------------------------

    /**
     * Normal: Smith and Lee wait; remove Smith and only Lee remains.
     */
    public void testRemoveParty()
    {
        restaurant.addParty("Smith", 2);
        restaurant.addParty("Lee", 2);
        restaurant.removeParty("Smith");
        assertEquals("Lee", waitingNames());
        assertEquals(3, listener.getCount());
    }

    /**
     * Bad input: removing "Nobody" does not crash the program and changes
     * nothing. The spec says this throws; HostController expects a false
     * return instead, so either response passes until the team picks one.
     */
    public void testRemovePartyNotWaiting()
    {
        restaurant.addParty("Smith", 2);
        try
        {
            restaurant.removeParty("Nobody");
        }
        catch (Exception e)
        {
            // throwing is one of the two allowed responses
        }
        assertEquals("Smith", waitingNames());
        assertEquals(1, listener.getCount());
    }

    // ----- freeTable --------------------------------------------------------

    /**
     * Normal: free table 1 after seating Smith, and table 1 is open again.
     */
    public void testFreeTable()
    {
        restaurant.addParty("Smith", 2);
        restaurant.seatAt(1);
        restaurant.freeTable(1);
        assertTrue(table(1).isFree());
        assertNull(table(1).getSeatedParty());
        assertEquals(3, listener.getCount());
    }

    /**
     * Bad input: freeing an already-free table does not crash, changes
     * nothing and sends no notification.
     */
    public void testFreeAlreadyFreeTable()
    {
        restaurant.freeTable(1);
        assertTrue(table(1).isFree());
        assertEquals(0, listener.getCount());
    }

    // ----- undoLastAdd ------------------------------------------------------

    /**
     * Normal: add Smith, then Lee; undo removes Lee and only Smith remains.
     */
    public void testUndoLastAdd()
    {
        restaurant.addParty("Smith", 2);
        restaurant.addParty("Lee", 2);
        restaurant.undoLastAdd();
        assertEquals("Smith", waitingNames());
        assertEquals(3, listener.getCount());
    }

    /**
     * Bad input: undo with nothing to undo does not crash the program,
     * changes nothing and sends no notification. The spec says this throws;
     * HostController expects a null return instead, so either passes.
     */
    public void testUndoOnEmptyList()
    {
        try
        {
            restaurant.undoLastAdd();
        }
        catch (Exception e)
        {
            // throwing is one of the two allowed responses
        }
        assertEquals(0, restaurant.getWaitingParties().size());
        assertEquals(0, listener.getCount());
    }

    // ----- getWaitingParties / getTables -------------------------------------

    /**
     * Normal: waiting parties come back in arrival order across size groups.
     */
    public void testGetWaitingPartiesArrivalOrder()
    {
        restaurant.addParty("Smith", 2);
        restaurant.addParty("Garcia", 6);
        restaurant.addParty("Kim", 4);
        restaurant.addParty("Lee", 1);
        assertEquals("Smith,Garcia,Kim,Lee", waitingNames());
    }

    /**
     * Bad input: editing the returned lists does not change the model (they
     * are copies). An unmodifiable list that refuses the edit is also fine.
     */
    public void testReturnedListsAreCopies()
    {
        restaurant.addParty("Smith", 2);
        try
        {
            restaurant.getWaitingParties().clear();
        }
        catch (UnsupportedOperationException e)
        {
            // read-only list: also protects the model
        }
        try
        {
            restaurant.getTables().clear();
        }
        catch (UnsupportedOperationException e)
        {
            // read-only list: also protects the model
        }
        assertEquals("Smith", waitingNames());
        assertEquals(8, restaurant.getTables().size());
    }

    // ----- seatCombined (Combine Tables feature) ------------------------------

    /**
     * Calls seatCombined and returns the exception it threw, or null.
     */
    private Exception tryCombine(int a, int b)
    {
        try
        {
            restaurant.seatCombined(a, b);
        }
        catch (Exception e)
        {
            return e;
        }
        return null;
    }


    /**
     * Normal: a party of 8 is seated at tables 5 + 6, both are taken, they
     * are joined, and the view is told once.
     */
    public void testSeatCombined()
    {
        restaurant.addParty("Patel", 8);
        int before = listener.getCount();
        Party seated = restaurant.seatCombined(5, 6);
        assertEquals("Patel", seated.getName());
        assertEquals("Patel", table(5).getSeatedParty().getName());
        assertEquals("Patel", table(6).getSeatedParty().getName());
        assertTrue(table(5).isJoined());
        assertEquals(6, table(5).getJoinedWith().getNumber());
        assertEquals(5, table(6).getJoinedWith().getNumber());
        assertEquals(0, restaurant.getWaitingParties().size());
        assertEquals(before + 1, listener.getCount());
    }


    /**
     * Bad input: nobody of 7-8 is waiting, so seatCombined returns null and
     * the tables stay free and apart.
     */
    public void testSeatCombinedNobodyLargeEnough()
    {
        restaurant.addParty("Smith", 2);
        int before = listener.getCount();
        assertNull(restaurant.seatCombined(5, 6));
        assertTrue(table(5).isFree());
        assertFalse(table(5).isJoined());
        assertEquals("Smith", waitingNames());
        assertEquals(before, listener.getCount());
    }


    /**
     * Bad input: the same table twice, a table that does not exist, and a
     * 2-top or the six-top all throw IllegalArgumentException and change
     * nothing.
     */
    public void testSeatCombinedBadTables()
    {
        restaurant.addParty("Patel", 8);
        int[][] bad = { { 5, 5 }, { 5, 99 }, { 0, 6 }, { 1, 5 }, { 7, 8 } };
        for (int[] pair : bad)
        {
            Exception thrown = tryCombine(pair[0], pair[1]);
            assertNotNull(pair[0] + "+" + pair[1] + " should be rejected",
                thrown);
            assertTrue(thrown instanceof IllegalArgumentException);
        }
        assertEquals("Patel", waitingNames());
        assertTrue(table(5).isFree());
    }


    /**
     * Bad input: combining with a taken table throws and the party keeps
     * waiting.
     */
    public void testSeatCombinedTakenTable()
    {
        restaurant.addParty("Kim", 4);
        restaurant.seatAt(5);
        restaurant.addParty("Patel", 8);
        Exception thrown = tryCombine(5, 6);
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalStateException);
        assertEquals("Patel", waitingNames());
        assertTrue(table(6).isFree());
    }


    /**
     * Normal: freeing either half of a combined table frees both halves and
     * splits them apart, with one notification.
     */
    public void testFreeCombinedTableFreesBoth()
    {
        restaurant.addParty("Patel", 8);
        restaurant.seatCombined(6, 7);
        int before = listener.getCount();
        assertTrue(restaurant.freeTable(7));
        assertTrue(table(6).isFree());
        assertTrue(table(7).isFree());
        assertFalse(table(6).isJoined());
        assertFalse(table(7).isJoined());
        assertEquals(before + 1, listener.getCount());
    }


    /**
     * Bad input: seatAt on one half of a combined table throws because it is
     * taken.
     */
    public void testSeatAtCombinedTableThrows()
    {
        restaurant.addParty("Patel", 8);
        restaurant.seatCombined(5, 6);
        restaurant.addParty("Kim", 4);
        Exception thrown = null;
        try
        {
            restaurant.seatAt(6);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertEquals("Kim", waitingNames());
    }


    /**
     * Normal: a party of 7-8 is never seated at a regular table, even the
     * six-top; it waits for a combine.
     */
    public void testLargePartyWaitsForCombine()
    {
        restaurant.addParty("Patel", 7);
        assertNull(restaurant.seatAt(8));
        assertTrue(table(8).isFree());
        assertEquals("Patel", waitingNames());
    }
}

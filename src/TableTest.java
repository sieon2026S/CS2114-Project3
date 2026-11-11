package model;

/**
 * Tests for Table. This test is in the model package because seat() and
 * clear() are package-private: only model classes may change a table.
 *
 * @author Charlie Horgan
 * @version 2026.11.11
 */
public class TableTest extends student.TestCase
{
    private Table table;
    private Party smith;

    /**
     * Runs before each test: one free two-top and one party of 2.
     */
    public void setUp()
    {
        table = new Table(1, 2);
        smith = new Party("Smith", 2, 1);
    }

    /**
     * Normal: a new table keeps its number and capacity and starts free.
     */
    public void testNewTableIsFree()
    {
        assertEquals(1, table.getNumber());
        assertEquals(2, table.getCapacity());
        assertTrue(table.isFree());
        assertNull(table.getSeatedParty());
    }

    /**
     * Bad input: a table number below 1 or a capacity other than 2, 4 or 6
     * throws IllegalArgumentException.
     */
    public void testBadTablesThrow()
    {
        int[][] badTables = { { 0, 2 }, { -1, 4 }, { 1, 0 }, { 1, 3 },
            { 1, 8 } };
        for (int[] bad : badTables)
        {
            Exception thrown = null;
            try
            {
                new Table(bad[0], bad[1]);
            }
            catch (Exception e)
            {
                thrown = e;
            }
            assertNotNull("table " + bad[0] + "/" + bad[1]
                + " should be rejected", thrown);
            assertTrue(thrown instanceof IllegalArgumentException);
        }
    }

    /**
     * Normal: seating a party marks the table taken by that party.
     */
    public void testSeat()
    {
        table.seat(smith);
        assertFalse(table.isFree());
        assertEquals(smith, table.getSeatedParty());
    }

    /**
     * Bad input: seating at a taken table throws IllegalStateException and
     * the first party stays seated (a party is never lost).
     */
    public void testSeatTakenTableThrows()
    {
        table.seat(smith);
        Exception thrown = null;
        try
        {
            table.seat(new Party("Lee", 2, 2));
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalStateException);
        assertEquals("Smith", table.getSeatedParty().getName());
    }

    /**
     * Bad input: seating null throws IllegalArgumentException and the
     * table stays free.
     */
    public void testSeatNullThrows()
    {
        Exception thrown = null;
        try
        {
            table.seat(null);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalArgumentException);
        assertTrue(table.isFree());
    }

    /**
     * Normal: clearing a taken table returns who left and frees it.
     */
    public void testClear()
    {
        table.seat(smith);
        assertEquals(smith, table.clear());
        assertTrue(table.isFree());
        assertNull(table.getSeatedParty());
    }

    /**
     * Bad input: clearing an already-free table returns null and changes
     * nothing.
     */
    public void testClearFreeTable()
    {
        assertNull(table.clear());
        assertTrue(table.isFree());
    }
}

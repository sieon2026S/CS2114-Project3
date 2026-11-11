package model;

/**
 * Tests for Waitlist, which owns the three size-group queues (small 1-2,
 * medium 3-4, large 5-6) and picks who a table gets. Covers the Test Plan
 * row "nextFor".
 *
 * @author Sathvik Verma
 * @version 2026.11.11
 */
public class WaitlistTest extends student.TestCase
{

    private Waitlist waitlist;

    /**
     * Runs before every test: an empty waitlist.
     */
    public void setUp()
    {
        waitlist = new Waitlist();
    }

    /**
     * Normal: Smith then Lee (both size 2) wait; a 2-top gets Smith first,
     * then Lee (first in, first out within a size group).
     */
    public void testNextForReturnsFirstArrival()
    {
        waitlist.add(new Party("Smith", 2, 1));
        waitlist.add(new Party("Lee", 2, 2));
        assertEquals("Smith", waitlist.nextFor(2).getName());
        assertEquals("Lee", waitlist.nextFor(2).getName());
    }

    /**
     * Normal: a 4-top gets the waiting party of 4 even though a party of 6
     * arrived earlier (the 6 cannot fit and keeps its place).
     */
    public void testNextForSkipsPartyThatDoesNotFit()
    {
        waitlist.add(new Party("Garcia", 6, 1));
        waitlist.add(new Party("Kim", 4, 2));
        assertEquals("Kim", waitlist.nextFor(4).getName());
        assertEquals("Garcia", waitlist.nextFor(6).getName());
    }

    /**
     * Bad input: no small party is waiting, so a 2-top gets null and the
     * larger party stays on the list.
     */
    public void testNextForNoFitReturnsNull()
    {
        waitlist.add(new Party("Garcia", 5, 1));
        assertNull(waitlist.nextFor(2));
        assertEquals("Garcia", waitlist.nextFor(6).getName());
    }

    /**
     * Bad input: nextFor on an empty waitlist returns null.
     */
    public void testNextForEmptyReturnsNull()
    {
        assertNull(waitlist.nextFor(2));
        assertNull(waitlist.nextFor(6));
    }

    /**
     * Bad input: adding null throws IllegalArgumentException.
     */
    public void testAddNullThrows()
    {
        Exception thrown = null;
        try
        {
            waitlist.add(null);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalArgumentException);
    }
}

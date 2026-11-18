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

    // ----- new for Combine Tables ----------------------------------------

    /**
     * Normal: capacity 8 (two combined 4-tops) gets the first party of 7-8,
     * in arrival order.
     */
    public void testNextForEightReturnsExtraLarge()
    {
        waitlist.add(new Party("Smith", 2, 1));
        waitlist.add(new Party("Patel", 8, 2));
        waitlist.add(new Party("Nguyen", 7, 3));
        assertEquals("Patel", waitlist.nextFor(8).getName());
        assertEquals("Nguyen", waitlist.nextFor(8).getName());
    }


    /**
     * Bad input: capacity 8 never takes a smaller party, so joined tables are
     * not wasted on a party that fits elsewhere.
     */
    public void testNextForEightNoExtraLargeReturnsNull()
    {
        waitlist.add(new Party("Smith", 2, 1));
        waitlist.add(new Party("Garcia", 6, 2));
        assertNull(waitlist.nextFor(8));
        assertEquals(2, waitlist.size());
    }


    /**
     * Bad input: a party of 8 does not fit the six-top, so the six-top skips
     * it and returns null.
     */
    public void testSixTopSkipsPartyOfEight()
    {
        waitlist.add(new Party("Patel", 8, 1));
        assertNull(waitlist.nextFor(6));
        assertEquals(1, waitlist.size());
    }


    /**
     * Normal: parties of 7-8 still appear in arrival order with everyone
     * else.
     */
    public void testWaitingInOrderIncludesExtraLarge()
    {
        waitlist.add(new Party("Smith", 2, 1));
        waitlist.add(new Party("Patel", 8, 2));
        waitlist.add(new Party("Kim", 4, 3));
        assertEquals("Patel", waitlist.getWaitingInOrder().get(1).getName());
        assertEquals(3, waitlist.size());
    }

    /**
     * Normal: adding parties while tables are being filled keeps each size
     * group first-in, first-out, even across groups.
     */
    public void testInterleavedAddAndSeatKeepsOrder()
    {
        waitlist.add(new Party("Ali", 2, 1));
        waitlist.add(new Party("Kim", 4, 2));
        assertEquals("Ali", waitlist.nextFor(2).getName());
        waitlist.add(new Party("Bo", 2, 3));
        waitlist.add(new Party("Lee", 4, 4));
        waitlist.add(new Party("Cy", 2, 5));
        assertEquals("Kim", waitlist.nextFor(4).getName());
        assertEquals("Bo", waitlist.nextFor(2).getName());
        waitlist.add(new Party("Di", 2, 6));
        assertEquals("Cy", waitlist.nextFor(2).getName());
        assertEquals("Lee", waitlist.getWaitingInOrder().get(0).getName());
        assertEquals("Di", waitlist.getWaitingInOrder().get(1).getName());
    }

    // ----- wait-time estimates (Zolo) ---------------------------------------

    /**
     * Normal: the fifth couple in line waits two 15-minute turns (four
     * 2-tops), and the party of 6 waits one turn (one six-top).
     */
    public void testEstimateWaitMinutes()
    {
        String[] names = { "Ann", "Bob", "Cal", "Dee", "Eve" };
        for (int i = 0; i < names.length; i++)
        {
            waitlist.add(new Party(names[i], 2, i + 1));
        }
        Party garcia = new Party("Garcia", 6, 6);
        waitlist.add(garcia);
        assertEquals(15, waitlist.estimateWaitMinutes(new Party("Ann", 2, 1)));
        assertEquals(30, waitlist.estimateWaitMinutes(new Party("Eve", 2, 5)));
        assertEquals(15, waitlist.estimateWaitMinutes(garcia));
    }


    /**
     * Bad input: null or a party who is not waiting returns -1.
     */
    public void testEstimateWaitMinutesNotWaiting()
    {
        waitlist.add(new Party("Ann", 2, 1));
        assertEquals(-1, waitlist.estimateWaitMinutes(null));
        assertEquals(-1, waitlist.estimateWaitMinutes(new Party("Zed", 2, 9)));
    }


    /**
     * Normal: a new party is quoted by how many of its size group are ahead.
     */
    public void testEstimateWaitForSize()
    {
        assertEquals(15, waitlist.estimateWaitForSize(2));
        waitlist.add(new Party("Patel", 8, 1));
        assertEquals(30, waitlist.estimateWaitForSize(7));
        assertEquals(15, waitlist.estimateWaitForSize(4));
    }
}

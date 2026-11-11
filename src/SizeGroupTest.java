package model;

/**
 * Tests for SizeGroup. SizeGroup decides which of the three waitlist
 * queues a party waits in and which tables a group fits at, so a wrong
 * boundary here would send parties to the wrong line.
 *
 * @author Charlie Horgan
 * @version 2026.11.11
 */
public class SizeGroupTest extends student.TestCase
{

    /**
     * Normal: every size from 1 to 6 maps to the right group, including
     * both edges of each group.
     */
    public void testForSizeEachGroup()
    {
        assertEquals(SizeGroup.SMALL, SizeGroup.forSize(1));
        assertEquals(SizeGroup.SMALL, SizeGroup.forSize(2));
        assertEquals(SizeGroup.MEDIUM, SizeGroup.forSize(3));
        assertEquals(SizeGroup.MEDIUM, SizeGroup.forSize(4));
        assertEquals(SizeGroup.LARGE, SizeGroup.forSize(5));
        assertEquals(SizeGroup.LARGE, SizeGroup.forSize(6));
    }

    /**
     * Bad input: zero, negative, too large and extreme sizes have no group
     * and throw IllegalArgumentException.
     */
    public void testForSizeBadSizesThrow()
    {
        int[] badSizes = { 0, -1, 7, 1000000000 };
        for (int size : badSizes)
        {
            Exception thrown = null;
            try
            {
                SizeGroup.forSize(size);
            }
            catch (Exception e)
            {
                thrown = e;
            }
            assertNotNull("size " + size + " should be rejected", thrown);
            assertTrue(thrown instanceof IllegalArgumentException);
        }
    }

    /**
     * Normal: each group fits a table at least as big as its largest
     * party.
     */
    public void testFitsAtBigEnoughTables()
    {
        assertTrue(SizeGroup.SMALL.fitsAt(2));
        assertTrue(SizeGroup.SMALL.fitsAt(4));
        assertTrue(SizeGroup.SMALL.fitsAt(6));
        assertTrue(SizeGroup.MEDIUM.fitsAt(4));
        assertTrue(SizeGroup.MEDIUM.fitsAt(6));
        assertTrue(SizeGroup.LARGE.fitsAt(6));
    }

    /**
     * Bad input: a group does not fit a table smaller than its largest
     * party, so a party of 4 is never offered a two-top.
     */
    public void testFitsAtTooSmallTables()
    {
        assertFalse(SizeGroup.SMALL.fitsAt(1));
        assertFalse(SizeGroup.SMALL.fitsAt(0));
        assertFalse(SizeGroup.MEDIUM.fitsAt(2));
        assertFalse(SizeGroup.LARGE.fitsAt(2));
        assertFalse(SizeGroup.LARGE.fitsAt(4));
    }
}

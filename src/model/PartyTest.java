package model;

/**
 * Tests for Party. The Party constructor is where the spec puts name and
 * size validation, so this covers the bad-input cases from our scope
 * (extreme, zero and negative sizes; empty and invalid names).
 *
 * @author Sathvik Verma
 * @version 2026.11.11
 */
public class PartyTest extends student.TestCase
{

    /**
     * Normal: a valid party keeps its name, size and arrival number.
     */
    public void testValidParty()
    {
        Party p = new Party("Smith", 2, 1);
        assertEquals("Smith", p.getName());
        assertEquals(2, p.getSize());
        assertEquals(1, p.getArrivalNumber());
    }

    /**
     * Normal: the smallest (1) and largest (6) sizes are allowed.
     */
    public void testSizeLimitsAllowed()
    {
        assertEquals(1, new Party("Lee", 1, 1).getSize());
        assertEquals(6, new Party("Garcia", 6, 2).getSize());
    }

    /**
     * Bad input: zero, negative, too large and extreme sizes all throw
     * IllegalArgumentException.
     */
    public void testBadSizesThrow()
    {
        int[] badSizes = { 0, -3, 7, 20, 1000000000 };
        for (int size : badSizes)
        {
            Exception thrown = null;
            try
            {
                new Party("Smith", size, 1);
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
     * Bad input: empty, blank, null and invalid-character names all throw
     * IllegalArgumentException (scope examples: "@d@m", "Z010").
     */
    public void testBadNamesThrow()
    {
        String[] badNames = { "", "   ", null, "@d@m", "Z010" };
        for (String name : badNames)
        {
            Exception thrown = null;
            try
            {
                new Party(name, 2, 1);
            }
            catch (Exception e)
            {
                thrown = e;
            }
            assertNotNull("name \"" + name + "\" should be rejected", thrown);
            assertTrue(thrown instanceof IllegalArgumentException);
        }
    }
}

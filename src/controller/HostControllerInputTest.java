package controller;

/**
 * Tests the controller's one input rule: turning raw text from the window's
 * boxes into whole numbers. This covers the scope's bad inputs that never
 * reach the model ("2.5", "four", numbers too big for an int, blank boxes)
 * without creating a window.
 *
 * @author Sathvik Verma
 * @version 2026.11.18
 */
public class HostControllerInputTest extends student.TestCase
{
    /**
     * Normal: whole numbers parse, with outer spaces ignored.
     */
    public void testParseWholeNumber()
    {
        assertEquals(Integer.valueOf(6), HostController.parseWholeNumber("6"));
        assertEquals(Integer.valueOf(5),
            HostController.parseWholeNumber("  5 "));
        assertEquals(Integer.valueOf(-3),
            HostController.parseWholeNumber("-3")); // the model rejects it
    }

    /**
     * Bad input: decimals, words, blank or missing text, and numbers too big
     * for an int all give null, so the controller shows a message instead of
     * crashing (scope: "double", "1000000000"-style input; Combine: blank or
     * "five").
     */
    public void testParseWholeNumberBadText()
    {
        String[] bad = { "2.5", "four", "five", "", "   ", null,
            "99999999999", "5+6", "6a" };
        for (String text : bad)
        {
            assertNull(HostController.parseWholeNumber(text));
        }
    }
}

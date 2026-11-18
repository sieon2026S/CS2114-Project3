package model;

/**
 * Tests for LinkedQueue, the singly linked FIFO queue that backs the
 * waitlist. Covers the Test Plan rows "enqueue / peek" and
 * "dequeue / isEmpty", plus remove(T), which undo and removeParty rely on.
 *
 * @author Sathvik Verma
 * @version 2026.11.11
 */
public class LinkedQueueTest extends student.TestCase
{

    private LinkedQueue<String> queue;

    /**
     * Runs before every test: a fresh, empty queue.
     */
    public void setUp()
    {
        queue = new LinkedQueue<String>();
    }

    // ----- enqueue / peek -------------------------------------------------

    /**
     * Normal: enqueue A then B, peek returns A and does not remove it.
     */
    public void testEnqueueThenPeekReturnsFirst()
    {
        queue.enqueue("A");
        queue.enqueue("B");
        assertEquals("A", queue.peek());
        assertEquals("A", queue.peek()); // peek again: still A
        assertEquals(2, queue.size());
    }

    /**
     * Bad input: enqueue(null) throws and the queue is unchanged.
     */
    public void testEnqueueNullThrows()
    {
        Exception thrown = null;
        try
        {
            queue.enqueue(null);
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(thrown instanceof IllegalArgumentException);
        assertTrue(queue.isEmpty());
    }

    /**
     * Bad input: peek on an empty queue does not crash. Returning null or
     * throwing are both fine (the spec does not pick one).
     */
    public void testPeekEmpty()
    {
        Object front = "unset";
        try
        {
            front = queue.peek();
        }
        catch (Exception e)
        {
            front = null; // throwing is also acceptable
        }
        assertNull(front);
        assertTrue(queue.isEmpty());
    }

    // ----- dequeue / isEmpty ----------------------------------------------

    /**
     * Normal: dequeue returns A, then B, and the queue is then empty.
     */
    public void testDequeueInFifoOrderThenEmpty()
    {
        assertTrue(queue.isEmpty());
        queue.enqueue("A");
        queue.enqueue("B");
        assertFalse(queue.isEmpty());
        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.dequeue());
        assertTrue(queue.isEmpty());
        assertEquals(0, queue.size());
    }

    /**
     * Normal: the queue keeps working after being emptied (tail is reset).
     */
    public void testReuseAfterEmptying()
    {
        queue.enqueue("A");
        queue.dequeue();
        queue.enqueue("C");
        assertEquals("C", queue.peek());
        assertEquals(1, queue.size());
    }

    /**
     * Bad input: dequeue on an empty queue throws.
     */
    public void testDequeueEmptyThrows()
    {
        Exception thrown = null;
        try
        {
            queue.dequeue();
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        assertTrue(queue.isEmpty());
    }

    // ----- remove(T): used by undoLastAdd and removeParty -------------------

    /**
     * Normal: removing the last item keeps order and moves the tail, so the
     * next enqueue lands at the back.
     */
    public void testRemoveLastMovesTail()
    {
        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");
        assertTrue(queue.remove("C"));
        queue.enqueue("D");
        assertEquals("A", queue.dequeue());
        assertEquals("B", queue.dequeue());
        assertEquals("D", queue.dequeue());
        assertTrue(queue.isEmpty());
    }

    /**
     * Normal: removing from the middle and the front keeps FIFO order.
     */
    public void testRemoveMiddleAndFront()
    {
        queue.enqueue("A");
        queue.enqueue("B");
        queue.enqueue("C");
        assertTrue(queue.remove("B"));
        assertTrue(queue.remove("A"));
        assertEquals("C", queue.peek());
        assertEquals(1, queue.size());
    }

    /**
     * Bad input: removing an item that is not there returns false and
     * changes nothing.
     */
    public void testRemoveMissingReturnsFalse()
    {
        queue.enqueue("A");
        assertFalse(queue.remove("Z"));
        assertFalse(queue.remove(null));
        assertEquals(1, queue.size());
        assertEquals("A", queue.peek());
    }

    // ----- interleaved enqueues and dequeues ---------------------------------

    /**
     * Normal: mixing enqueues and dequeues keeps strict first-in, first-out
     * order and the right size at every step.
     */
    public void testInterleavedEnqueueDequeueKeepsOrder()
    {
        queue.enqueue("A");
        queue.enqueue("B");
        assertEquals("A", queue.dequeue());
        queue.enqueue("C");
        queue.enqueue("D");
        assertEquals("B", queue.dequeue());
        assertEquals("C", queue.peek());
        queue.enqueue("E");
        assertEquals(3, queue.size());
        assertEquals("C", queue.dequeue());
        assertEquals("D", queue.dequeue());
        queue.enqueue("F");
        assertEquals("E", queue.dequeue());
        assertEquals("F", queue.dequeue());
        assertTrue(queue.isEmpty());
    }


    /**
     * Bad input: draining the queue in the middle of interleaving, then
     * dequeuing once too many, throws, and the queue still works afterwards
     * (the tail was reset).
     */
    public void testInterleavedOverDequeueThenReuse()
    {
        queue.enqueue("A");
        assertEquals("A", queue.dequeue());
        queue.enqueue("B");
        assertEquals("B", queue.dequeue());
        Exception thrown = null;
        try
        {
            queue.dequeue();
        }
        catch (Exception e)
        {
            thrown = e;
        }
        assertNotNull(thrown);
        queue.enqueue("C");
        queue.enqueue("D");
        assertEquals("C", queue.dequeue());
        assertEquals("D", queue.peek());
        assertEquals(1, queue.size());
    }
}

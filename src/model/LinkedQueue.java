package model;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A singly linked first-in, first-out queue. Knows nothing about parties or
 * tables.
 *
 * @author Zolo (fixes and toList() by Sathvik Verma)
 * @version 2026.11.11
 * @param <T> the type of item stored
 */
public class LinkedQueue<T> {
    /**
     * One node in the chain.
     *
     * @param <T> the type of item stored
     */
    public static class Node<T> {
        public T data;
        public Node<T> next;

        /**
         * @param data the item to hold
         */
        public Node(T data) {
            this.data = data;
            this.next = null;
        }

        /**
         * @param data the item to hold
         * @param next the following node
         */
        public Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Creates an empty queue.
     */
    public LinkedQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Adds an item at the back.
     *
     * @param item the item to add
     * @throws IllegalArgumentException if item is null
     */
    public void enqueue(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot enqueue null.");
        }
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
        }
        else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Removes and returns the front item.
     *
     * @return the front item
     * @throws NoSuchElementException if the queue is empty
     */
    public T dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("Queue is empty");
        }
        T data = head.data;
        head = head.next;
        size--;
        if (head == null) {
            tail = null;
        }
        return data;
    }

    /**
     * @return the front item without removing it, or null if empty
     */
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }

    /**
     * @return true if the queue has no items
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * @return the number of items
     */
    public int size() {
        return size;
    }

    /**
     * Removes the first item equal to the given one from anywhere in the
     * queue. Used for walk-aways and undo, never for seating.
     *
     * @param item the item to remove
     * @return true if it was found and removed
     */
    public boolean remove(T item) {
        if (isEmpty() || item == null) {
            return false;
        }
        if (head.data.equals(item)) {
            dequeue();
            return true;
        }
        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(item)) {
                current.next = current.next.next;
                if (current.next == null) {
                    tail = current;
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * @return a new list of the items, front to back (queue unchanged)
     */
    public List<T> toList() {
        List<T> list = new ArrayList<>();
        for (Node<T> n = head; n != null; n = n.next) {
            list.add(n.data);
        }
        return list;
    }
}

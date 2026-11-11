package model;

import java.util.NoSuchElementException;

public class LinkedQueue<T> {
    public static class Node<T> {
        public T data;
        public Node<T> next;
        
        public Node(T data) {
            this.data = data;
            this.next = null;
        }
        public Node(T data, Node<T> next) {
            this.data = data;
            this.next = next;
        }
    }
    private Node<T> head;
    private Node<T> tail;
    private int size;
    
    public LinkedQueue() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }
    
    public void enqueue (T item) {
        Node<T> newNode = new Node<>(item);
        if (isEmpty()) {
            head = newNode;
            tail = newNode;
            
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }
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
    public T peek() {
        if (isEmpty()) {
            return null;
        }
        return head.data;
        
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public int size() {
        return size;
    }
    public boolean remove(T item) {
        if (isEmpty()) return false;
        
        if (head.data.equals(item)) {
            dequeue();
            return true;
        }
        Node<T> current = head;
        while(current.next!=null) {
            if(current.next.data.equals(item)) {
                current.next = current.next.next;
                if(current.next == null) {
                    tail = current;
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }
}

  

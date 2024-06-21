package queue;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeQueue<T> implements Queueable<T> {

    private final Node<T> dummy = new Node<>(null);
    private final AtomicReference<Node<T>> head = new AtomicReference<>(dummy);
    private final AtomicReference<Node<T>> tail = new AtomicReference<>(dummy);

    public void enqueue(T value) {
        Node<T> newNode = new Node<>(value);
        while (true) {
            Node<T> curTail = tail.get();
            Node<T> tailNext = curTail.next.get();
            if (curTail == tail.get()) {
                if (tailNext != null) {
                    // Tail was not pointing to the last node
                    tail.compareAndSet(curTail, tailNext);
                } else {
                    if (curTail.next.compareAndSet(null, newNode)) {
                        // Insertion succeeded, moving the tail to the inserted node
                        tail.compareAndSet(curTail, newNode);
                        return;
                    }
                }
            }
        }
    }

    public T dequeue() {
        while (true) {
            Node<T> oldHead = head.get();
            Node<T> oldTail = tail.get();
            Node<T> oldHeadNext = oldHead.next.get();
            if (oldHead == head.get()) {
                if (oldHead == oldTail) {
                    if (oldHeadNext == null) {
                        return null; // Queue is empty
                    }
                    // Tail is behind, advancing it
                    tail.compareAndSet(oldTail, oldHeadNext);
                } else {
                    T value = oldHeadNext.value;
                    if (head.compareAndSet(oldHead, oldHeadNext)) {
                        // Dequeue successful
                        return value;
                    }
                }
            }
        }
    }
}
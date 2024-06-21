package queue;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SimpleBlockingQueue<T> implements Queueable<T> {
    private final Queue<T> queue = new LinkedList();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private volatile boolean done = false;

    public void enqueue(T element) {
        lock.lock();
        try {
            queue.add(element);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public T dequeue() throws InterruptedException {
        lock.lock();
        try {
            while(queue.isEmpty() && !done) {
                notEmpty.await();
            }
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    public void setDone() {
        lock.lock();
        try {
            done = true;
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }
}

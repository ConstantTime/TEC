package queue;

interface Queueable<T> {
        void enqueue(T item);
        T dequeue() throws InterruptedException;
    }
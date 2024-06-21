package queue;

import java.util.concurrent.atomic.AtomicInteger;

public class Benchmark {
    public static void main(String[] args) throws InterruptedException {
        final int[] threadCounts = {1, 2, 4, 8, 16};
        final int[] operationsPerThreadOptions = {1000, 10000, 100000};

        System.out.println("QueueType,Threads,OperationsPerThread,Time(ms)");

        for (int numThreads : threadCounts) {
            for (int operationsPerThread : operationsPerThreadOptions) {
                // Benchmarking SimpleBlockingQueue
                SimpleBlockingQueue<Integer> blockingQueue = new SimpleBlockingQueue<>();
                long blockingQueueTime = benchmarkQueue(blockingQueue, numThreads, operationsPerThread);
                System.out.println(String.format("SimpleBlockingQueue,%d,%d,%d", numThreads, operationsPerThread, blockingQueueTime));

                // Benchmarking LockFreeQueue
                LockFreeQueue<Integer> lockFreeQueue = new LockFreeQueue<>();
                long lockFreeQueueTime = benchmarkQueue(lockFreeQueue, numThreads, operationsPerThread);
                System.out.println(String.format("LockFreeQueue,%d,%d,%d", numThreads, operationsPerThread, lockFreeQueueTime));
            }
        }
    }

    static long benchmarkQueue(Queueable<Integer> queue, int numThreads, int operationsPerThread) throws InterruptedException {
        AtomicInteger operationCount = new AtomicInteger(operationsPerThread);
        Thread[] threads = new Thread[numThreads * 2]; // Twice the number for producers and consumers
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numThreads * 2; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int op = 0; op < operationCount.get(); op++) {
                    if (finalI % 2 == 0) {
                        queue.enqueue(op);
                    } else {
                        try {
                            queue.dequeue();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        for (Thread t : threads) {
            t.start();
        }

        for (Thread t : threads) {
            t.join();
        }

        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

}
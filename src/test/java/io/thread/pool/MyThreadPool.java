package io.thread.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MyThreadPool extends Thread {

    private static volatile int seq = 0; //�߳����

    private final static LinkedList<Runnable> TASK_QUEUE = new LinkedList<>(); //�������

    private final static List<Worker> THREAD_COLLECTION = new ArrayList<>();   //�̼߳���

    private int size; //��ǰ�߳�����

    private int min;  //��С�߳�����

    private int corePoolSize; //�����߳�����
    
    private int max;  //����߳�����

    private final int queueSize;  // ������д�С

    private final DiscardPolicy discardPolicy;
    
    private volatile boolean destroy = false;
    
    private final static ThreadGroup GROUP = new ThreadGroup("Pool_Group");
    
    public MyThreadPool() {
        this(4, 8, 12, 100, () -> {
            throw new DiscardException("Discard This Runnable.");
        });
    }

    public MyThreadPool(int min, int corePoolSize, int max, int queueSize, DiscardPolicy discardPolicy) {
        this.min = min;
        this.corePoolSize = corePoolSize;
        this.max = max;
        this.queueSize = queueSize;
        this.discardPolicy = discardPolicy;
        init();
    }

    private void init() {
        for (int i = 0; i < this.min; i++) {
            createWorker();
        }
        this.size = min;
        this.start();
    }

    //�ύ����
    public void submit(Runnable runnable) {
        if (destroy)
            throw new IllegalStateException("The thread pool already destroy and not allow submit task.");

        synchronized (TASK_QUEUE) {
            if (TASK_QUEUE.size() > queueSize)
                discardPolicy.discard();
            TASK_QUEUE.addLast(runnable);
            TASK_QUEUE.notifyAll();
        }
    }

    @Override
    public void run() {
        while (!destroy) {
            System.out.printf("Pool#Min:%d,corePoolSize:%d,Max:%d,Current:%d,QueueSize:%d\n",
                    this.min, this.corePoolSize, this.max, this.size, TASK_QUEUE.size());
            try {
                //����������е������������ʱ�������߳���
                Thread.sleep(5_000L);
                if (TASK_QUEUE.size() > corePoolSize && size < corePoolSize) {
                    for (int i = size; i < corePoolSize; i++) {
                        createWorker();
                    }
                    System.out.println("The pool has incremented to corePoolSize.");
                    size = corePoolSize;
                } else if (TASK_QUEUE.size() > max && size < max) {
                    for (int i = size; i < max; i++) {
                        createWorker();
                    }
                    System.out.println("The pool has incremented to max.");
                    size = max;
                }
                //�������Ϊ�գ������������߳��������̹߳ر�
                synchronized (THREAD_COLLECTION) {
                    if (TASK_QUEUE.isEmpty() && size > corePoolSize) {
                        System.out.println("=========Reduce========");
                        int releaseSize = size - corePoolSize;
                        for (Iterator<Worker> it = THREAD_COLLECTION.iterator(); it.hasNext(); ) {
                            if (releaseSize <= 0)
                                break;
                            Worker worker = it.next();
                            if(worker.getTaskState() == TaskState.BLOCKED) {
                                worker.close();
                                worker.interrupt();
                                releaseSize--;
                            }
                            it.remove();
                        }
                        size = corePoolSize;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void createWorker() {
        Worker worker = new Worker(GROUP, "THREAD_POOL-" + (++seq));
        worker.start();
        THREAD_COLLECTION.add(worker);
    }
    
    //�̵߳İ�װ��
    private static class Worker extends Thread {

        private volatile TaskState taskState = TaskState.FREE;

        public Worker(ThreadGroup group, String name) {
            super(group, name);
        }

        public TaskState getTaskState() {
            return this.taskState;
        }

        public void run() {
            OUTER:
            while (this.taskState != TaskState.DEAD) {
                Runnable runnable;
                synchronized (TASK_QUEUE) {
                    while (TASK_QUEUE.isEmpty()) {
                        try {
                            taskState = TaskState.BLOCKED;
                            TASK_QUEUE.wait();
                        } catch (InterruptedException e) {
                            System.out.println("Closed.");
                            break OUTER;
                        }
                    }
                    runnable = TASK_QUEUE.removeFirst();
                }

                if (runnable != null) {
                    taskState = TaskState.RUNNING;
                    runnable.run();
                    taskState = TaskState.FREE;
                }
            }
        }

        public void close() {
            this.taskState = TaskState.DEAD;
        }
    }
    
    //�Զ���״̬
    private enum TaskState {
        FREE, RUNNING, BLOCKED, DEAD
    }
    
    //�ܾ������׳��쳣
    public static class DiscardException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public DiscardException(String message) {
            super(message);
        }
    }
    //�ܾ�����
    public interface DiscardPolicy {

        void discard() throws DiscardException;
    }
    
    //�ر��̳߳�
    public void shutdown() throws InterruptedException {
        while (!TASK_QUEUE.isEmpty()) { //������в�Ϊ�գ������ȴ�
            Thread.sleep(50);
        }
        synchronized (THREAD_COLLECTION) {
            int initVal = THREAD_COLLECTION.size();
            while (initVal > 0) {
                for (Worker worker : THREAD_COLLECTION) {
                    if (worker.getTaskState() == TaskState.BLOCKED) { //�����blocked״̬��ֱ�ӹر�
                        worker.close();
                        worker.interrupt();
                        initVal--;
                    } else {   //����״̬���͵ȴ�10����
                        Thread.sleep(10);
                    }
                }
            }
        }
        this.destroy = true;
        System.out.println("The thread pool is shutdown");
    }


    public static void main(String[] args) throws InterruptedException {
        MyThreadPool threadPool = new MyThreadPool();
        for (int i = 1; i <= 40; i++) {
            threadPool.submit(new MyRunnable(i));
        }

        Thread.sleep(40000);
        threadPool.shutdown();

    }
    
    public static class MyRunnable  implements Runnable {
        int index ;
        public  MyRunnable(int index) {
            this.index = index;
        }
        @Override
        public void run() {
            System.out.println("The runnable "+ index +" be serviced by " + Thread.currentThread() + " start.");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("The runnable "+ index +" be serviced by " + Thread.currentThread() + " finished.");
        }
        
    }
    
    
}

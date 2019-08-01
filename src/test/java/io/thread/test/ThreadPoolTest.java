package io.thread.test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class ThreadPoolTest {
	
	@Test
	public void testPool() throws InterruptedException {
		
		int coreSize = 2;
		int maxSize = 4;
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(2);
		
		ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(coreSize, maxSize, 100L, 
				TimeUnit.MILLISECONDS, queue, new MyThreadFactory(), new MyPloicy());
		
		for(int i = 0 ; i< 10 ; i++) {
			poolExecutor.execute(new MyRunnable(String.valueOf(i)));
		}
		
		TimeUnit.SECONDS.sleep(30);
	}
}

class MyPloicy implements RejectedExecutionHandler {

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		// TODO Auto-generated method stub
		log((MyRunnable) r);
	}
	
	private void log(MyRunnable r) {
		System.out.println("任务被拒绝了 - "+ r.id);
	}
}

class MyThreadFactory implements ThreadFactory {

	private static AtomicInteger intgger = new AtomicInteger(1);
	
	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		return new Thread(r, "Thread-" + intgger.incrementAndGet());
	}
}

class MyRunnable implements Runnable {

	String id;
	
	public MyRunnable(String id) {
		// TODO Auto-generated constructor stub
		this.id = id;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		System.out.println("任务被执行了 - "+ id );
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}

package io.thread.test;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import io.thread.pool.MyThreadPoolExecutor;

public class MyThreadPoolTest {
	
	@Test
	public void poolTest() throws InterruptedException {
		int coreSize = 2;
		int maxSize = 4;
		BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(2);
		
		MyThreadPoolExecutor poolExecutor = new MyThreadPoolExecutor(coreSize, maxSize, 100L, 
				TimeUnit.MILLISECONDS, queue, new MyThreadFactoryTest(), new MyPloicyTest());
		
		for(int i = 0 ; i< 10 ; i++) {
			poolExecutor.execute(new MyRunnableTest(String.valueOf(i)));
		}
		
		try {
			System.in.read();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}


class MyPloicyTest implements MyRejectedExecutionHandler {

	@Override
	public void rejectedExecution(Runnable r, MyThreadPoolExecutor executor) {
		// TODO Auto-generated method stub
		log((MyRunnableTest) r);
	}

	private void log(MyRunnableTest r) {
		System.out.println("任务被拒绝了 - "+ r.id);
	}
}

class MyThreadFactoryTest implements ThreadFactory {

	private static AtomicInteger intgger = new AtomicInteger(1);
	
	@Override
	public Thread newThread(Runnable r) {
		// TODO Auto-generated method stub
		return new Thread(r, "Thread-" + intgger.incrementAndGet());
	}
}

class MyRunnableTest implements Runnable {

	String id;
	
	public MyRunnableTest(String id) {
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
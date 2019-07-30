package io.thread.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;


public class Counter {
	
	private AtomicInteger inter = new AtomicInteger(0);
	
	private int i = 0 ;

	@Test
	public void testCounter() {
		final Counter counter = new Counter();
		CountDownLatch cDownLatch = new CountDownLatch(100);
		
		
		for (int i = 0 ; i < 100 ; i++) {
			Thread thread = new Thread(new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					for(int j = 0 ; j < 1000 ; j++) {
						counter.safeCount();
						counter.count();
					}
					cDownLatch.countDown();
				}
			});
			thread.start();
		}	
		
		
		try {
			cDownLatch.await();
			System.out.println(counter.i);
			System.out.println(counter.inter.get());
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	protected synchronized void count() {
		// TODO Auto-generated method stub
		i++;
	}

	protected void safeCount() {
		// TODO Auto-generated method stub
		for(;;) {
			int i = inter.get();
			boolean res = inter.compareAndSet(i, ++i);
			if(res) {
				break;
			}
		}
	}
}

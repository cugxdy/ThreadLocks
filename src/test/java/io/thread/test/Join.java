package io.thread.test;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class Join { 
	
	@Test
	public void main() throws InterruptedException {
		Thread preThread = Thread.currentThread();
		for(int i = 1 ;i <= 10 ;i++) {
			Thread thread = new Thread(new JoinTest(preThread), String.valueOf(i));
			thread.start();
			preThread = thread;
		}
		TimeUnit.SECONDS.sleep(5);
		System.out.println(Thread.currentThread().getName() + " terminate.");
	}
	
	private static class JoinTest implements Runnable {

		private Thread thread;
		
		public JoinTest(Thread preThread) {
			// TODO Auto-generated constructor stub
			this.thread = preThread;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			System.out.println("sss");
			try {
				thread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(Thread.currentThread().getName() + " terminate.");
			
		}
	}

}

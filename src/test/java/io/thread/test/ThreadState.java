package io.thread.test;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class ThreadState {
	
	
	@Test
	public void testState() {
		Thread thread1 = new Thread(new TimeWait(), "TimeWait");
		thread1.start();
		Thread thread2 = new Thread(new WaitState() , "WaitState");
		thread2.start();
		Thread thread3 = new Thread(new synState() , "synState11");
		thread3.start();
		Thread thread4 = new Thread(new synState() , "synState22");
		thread4.start();
		
		try {
			TimeUnit.SECONDS.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	private class TimeWait implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!Thread.currentThread().isInterrupted()) {
				try {
					TimeUnit.SECONDS.sleep(10);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
	}
	
	private class WaitState implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!Thread.currentThread().isInterrupted()) {
				synchronized (WaitState.class) {
					try {
						WaitState.class.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	
	private class synState implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				synchronized (synState.class) {
					try {
						TimeUnit.SECONDS.sleep(10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		
	}
	

}

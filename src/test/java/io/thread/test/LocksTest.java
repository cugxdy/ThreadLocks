package io.thread.test;

import org.junit.Test;

import cugxdy.util.concurrent.locks.ReentrantLock;

public class LocksTest {
	@Test
	public void test() {
		ReentrantLock lock = new ReentrantLock();
		lock.lock();
		System.out.println("test");
		lock.unlock();
		
	}

}

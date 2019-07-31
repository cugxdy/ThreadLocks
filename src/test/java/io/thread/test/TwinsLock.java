package io.thread.test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class TwinsLock implements Lock{

	private Sync sync;
	
	private static final class Sync extends AbstractQueuedSynchronizer{
		
		@Override
		public int tryAcquireShared(int arg) {
			for(;;) {
				int current = getState();
				int newCount = current - arg;
				if(newCount < 0 || compareAndSetState(current, newCount)) {
					return newCount;
				}
			}
		}
		
		@Override
		public boolean tryReleaseShared(int arg) {
			for(;;) {
				int current = getState();
				int newCount = current + arg;
				if(compareAndSetState(current, newCount)) {
					return true;
				}
			}
		}
		
	}
	
	
	@Override
	public void lock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean tryLock() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void unlock() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Condition newCondition() {
		// TODO Auto-generated method stub
		return null;
	}

}

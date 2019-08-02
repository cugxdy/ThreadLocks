package io.thread.pool;

import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyThreadPoolExecutor extends AbstractExecutorService{

	
	private final AtomicInteger ctl = new AtomicInteger(ctlOf(0, 0));
	
	private static final int COUNT_BITS = Integer.SIZE - 3;
	
	private static final int CAPACITY = (1 << COUNT_BITS) - 1;
	
	private static final int RUNNING =  -1 << COUNT_BITS;
	
	private static final int SHUTDOWN = 0 << COUNT_BITS;
	
	private static final int STOP = 1 << COUNT_BITS;
	
	private static final int TIDYING = 2 << COUNT_BITS;
	
	private static final int TERMINATED = 3 << COUNT_BITS;
	
	private static int runStateOf(int c) { return c & ~CAPACITY; }
	
	private static int workerCountOf(int c) { return c & CAPACITY; }

	private static int ctlOf(int s , int r) {return s | r;}
	
	private static boolean runStateLessThan(int c , int s) {
		return c < s;
	}
	
	private static boolean runStateAtLeast(int c , int s) {
		return c >= s;
	}
	
	// 判断当前线程池状态是否是RUNNING
	private static boolean isRunning(int c) {
		return c < SHUTDOWN;
	}
	
    /**
     * Attempts to CAS-increment the workerCount field of ctl.
     */
    // 原子性的递增workerCount属性值 (增加线程计数,低位加法)
	private boolean compareAndIncrementWorkerCount(int expect) {
		return ctl.compareAndSet(expect, expect + 1);
	}
	
    /**
     * Attempts to CAS-decrement the workerCount field of ctl.
     */
    // 原子性的递减workerCount属性值 (减少线程计数,低位减法)
	private boolean compareAndDecrementWorkerCount(int expect) {
		return ctl.compareAndSet(expect, expect - 1); 
	}
	
    // 原子性的递减workerCount属性值 (减少线程计数,低位减法)
	private void decrementWorkerCount() {
		do {
			
		} while (!compareAndDecrementWorkerCount(ctl.get()));
	}
	
	private final BlockingQueue<Runnable> workQueue;
	
	private final ReentrantLock mainLock = new ReentrantLock();
	
	private final HashSet<Worker> workers = new HashSet<Worker>();
	 
	private final Condition termination = mainLock.newCondition();
	
	private int largestPoolSize;
	
	private long completedTaskCount;
	
	private volatile ThreadFactory threadFactory;
	
	private volatile RejectedExecutionHandler handler;
	
	private volatile long keepAliveTime;
	
	private volatile boolean allowCoreThreadTimeOut;
	
	private volatile int corePoolSize;
	
	private volatile int maximumPoolSize;
	
	private static final RejectedExecutionHandler defaultHandler = new ThreadPoolExecutor.AbortPolicy();
	
    private static final RuntimePermission shutdownPerm =
            new RuntimePermission("modifyThread");

    /* The context to be used when executing the finalizer, or null. */
    private final AccessControlContext acc;
	
	public ThreadFactory getThreadFactory() {
		return threadFactory;
	}
    
	private final class Worker extends AbstractQueuedSynchronizer  implements Runnable{
		private static final long serialVersionUID = 6138294804551838833L;

		final Thread thread;
		
		Runnable firstTask;
		
		volatile long completedTasks;
		
		public Worker(Runnable firstTask) {
			setState(-1);
			this.firstTask = firstTask;
			thread = getThreadFactory().newThread(this);
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			runWorker(this);
		}
		
		
		protected boolean isHeldExclusively() {
			return getState() != 0;
		}
		
		protected boolean tryAcquire(int c) {
			if(compareAndSetState(0, 1)) {
				setExclusiveOwnerThread(Thread.currentThread());
				return true;
			}
			return false;
		}
		
		protected boolean tryRelease(int c) {
			setExclusiveOwnerThread(null);
			setState(0);
			return true;
		}
		
		public void lock() { acquire(1);}
		
		public boolean tryLock() { return tryAcquire(1);}
		
		public void unlock() { release(1);}
		
		public boolean isLocked() { return isHeldExclusively(); }
		
		void interruptIfStarted() {
			Thread t;
			if(getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
				try {
					t.interrupt();
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		}
		
	}
	
	private void advanceRunState(int targetState) {
		for(;;) {
			int c = ctl.get();
			if(runStateAtLeast(c,targetState) || 
					ctl.compareAndSet(c, ctlOf(targetState,workerCountOf(c)))) {
				break;
			}
		}
	}
	
	private static final boolean ONLY_ONE = true;
	
	final void tryTerminate() {
		for(;;) {
			int c = ctl.get();
			
			if(isRunning(c) || runStateAtLeast(c, TIDYING)
					|| (runStateOf(c) == SHUTDOWN) || !workQueue.isEmpty()) {
				return;
			}
			
			if(workerCountOf(c) != 0) {
				interruptIdleWorkers(ONLY_ONE);
				return;
			}
			
			final ReentrantLock lock = this.mainLock;
			lock.lock();
			try {
				if(ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {
					try {
                    	// 什么都不做
                        terminated();
					} finally {
						// TODO: handle finally clause
						ctl.set(ctlOf(TERMINATED, 0));
						termination.signalAll();
					}
					return;
				}
			} finally {
				// TODO: handle finally clause
				lock.unlock();
			}
		}
	}
	
    // 检查是否存在shutdown权限
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
            	// 检测shutdown权限
                for (Worker w : workers)
                    security.checkAccess(w.thread);
            } finally {
                mainLock.unlock();
            }
        }
    }
    
    private void interruptWorkers() {
    	final ReentrantLock lock = this.mainLock;
    	lock.lock();
    	try {
			for(Worker worker : workers) {
				worker.interruptIfStarted();
			}
		} finally {
			// TODO: handle finally clause
			lock.unlock();
		}
    }
    
    private void interruptIdleWorkers() {
    	interruptIdleWorkers(false);
    }
    
    final void reject(Runnable runnable) {
    	// handler.rejectedExecution(runnable, this);
    	System.out.println("拒绝执行");
    }
    
    void onShutdown() {
    }
    
    final boolean isRunningOrShutdown(boolean shutdownOk) {
    	int c = runStateOf(ctl.get());
    	return isRunning(c) || (c == SHUTDOWN && shutdownOk);
    }
    
    // ????????????????????????????????????????????????????
    private List<Runnable> drainQueue() {
    	// 获取任务队列
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        
        // 判断workQueue释放为空
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
            	// 将Runnable对象添加至ArrayList对象中并从workQueue中删除掉
                if (q.remove(r))
                    taskList.add(r);
            }
        }
        return taskList;
    }
    
	private void terminated() {
		// TODO Auto-generated method stub
		
	}

	private void interruptIdleWorkers(boolean onlyOne) {
		// TODO Auto-generated method stub
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		try {
			for(Worker worker : workers ) {
				if(worker.thread != null && !worker.thread.isInterrupted() 
						&& worker.tryLock()) {
					try {
						worker.thread.interrupt();
					} catch (Exception e) {
						// TODO: handle exception
					}finally {
						worker.unlock();
					}
				}
				if(onlyOne) {
					break;
				}
			}
		} finally {
			// TODO: handle finally clause
			lock.unlock();
		}
	}
	
	
	private boolean addWorker(Runnable command , boolean core) {
		retry:
			for (;;) {
				int c = ctl.get();
				int rs = runStateOf(c);
				
				if(rs >= SHUTDOWN && !(rs == SHUTDOWN && 
						command == null && !workQueue.isEmpty())) {
					return false;
				}
				
				for(;;) {
					int wc = workerCountOf(c);
					if(wc > CAPACITY ||
							wc > (core ? corePoolSize : maximumPoolSize)) {
						return false;
					}
					
					if(compareAndIncrementWorkerCount(c)) {
						break retry;
					}
					
					if(runStateOf(ctl.get()) != rs) {
						continue retry;
					}
				}
			}
		
		// 三：新建新的Worker并执行
    	boolean workerStarted = false;
    	boolean workerAdded = false;
    	Worker w = null;
    	try {
			w = new Worker(command);
			final Thread t = w.thread;
			if(t != null) {
				final ReentrantLock lock = this.mainLock;
				lock.lock();
				try {
					int c = runStateOf(ctl.get());
					if(c < SHUTDOWN || (c == SHUTDOWN && command == null)) {
						if(t.isAlive()) {
							throw new IllegalThreadStateException();
						}
						
						workers.add(w);
						int size = workers.size();
						if (size > largestPoolSize) {
							largestPoolSize = size;
						}
						workerAdded = true;
					}	
				} finally {
					// TODO: handle finally clause
					lock.unlock();
				}
				if(workerAdded) {
					t.start();
					workerStarted = true;
				}
				
			}
		} finally {
			// w : handle finally clause
			if(!workerStarted) {
				addWorkerFailed(w);
			}
		}
    	return workerStarted;
	}
	

	private void addWorkerFailed(Worker w) {
		// TODO Auto-generated method stub
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		try {
			if(w != null) {
				workers.remove(w);
			}
			decrementWorkerCount();
			tryTerminate();
		} finally {
			// TODO: handle finally clause
			lock.unlock();
		}
	}
	
	private void processWorkerExit(Worker w, boolean completedAbruptly) {
		if(completedAbruptly) {
			decrementWorkerCount();
		}
		
		final ReentrantLock lock = this.mainLock;
		lock.lock();
		try {
			completedTaskCount += w.completedTasks;
			workers.remove(w);
		} finally {
			// TODO: handle finally clause
			lock.unlock();
		}
		
		tryTerminate();
		
		int c = ctl.get();
		
		if(runStateLessThan(c, STOP)) {
			if(!completedAbruptly) {
				int min = allowCoreThreadTimeOut ? 0 : corePoolSize;
				if(min == 0 && !workQueue.isEmpty()) {
					min = 1;
				}
				if(workerCountOf(c) >= min) {
					return;
				}
			}
			addWorker(null, false);
		}
	}
	
	private Runnable getTask() {
		
		boolean timedOut = false;
		for(;;) {
			
			int c = ctl.get();
			
			int rs = runStateOf(c);
			
			if(rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
				decrementWorkerCount();
				return null;
			}
			
			int wc = workerCountOf(c);
			
			boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;
			
			if((wc > maximumPoolSize || (timed && timedOut))
					&& (wc > 1 || workQueue.isEmpty() )) {
            	// 递减工作线程计数,并返回null
                if (compareAndDecrementWorkerCount(c))
                    return null;
                continue;
			}
			 
			try {
				
				Runnable r = timed ? 
						workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) : 
							workQueue.take();
						
				if(r != null) {
					return r;
				}
				
				timedOut = true;
			} catch (InterruptedException e) {
				// TODO: handle exception
				timedOut = false;
			}
		}
	}
	
	

	final void runWorker(Worker worker) {
		// TODO Auto-generated method stub
		Thread  wt = Thread.currentThread();
		Runnable task = worker.firstTask;
		worker.firstTask = null;
		
		worker.unlock();
		boolean completedAbruptly = true;
		try {
			while(task != null || ( (task = getTask())!=null)) {
				worker.lock();
				if((runStateAtLeast(ctl.get(), STOP) || 
						(Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) && 
						!wt.isInterrupted()) {
					wt.interrupt();
				}
				
				try {
					beforeExecute(wt, task);
					Throwable thrown = null;
					try {
						task.run();
					} catch (RuntimeException e) {
						// TODO: handle exception
						thrown = e ; throw e;
					} finally {
						afterExecute(task,thrown);
					}
				} finally {
					task = null; // 为了辅助GC
					// TODO: handle finally clause
					worker.completedTasks++;
					worker.unlock();
				}
			}
			completedAbruptly = false;
		} finally {
			// TODO: handle exception
			processWorkerExit(worker, completedAbruptly);
		}
	}
	
	private void afterExecute(Runnable task, Throwable thrown) {
		// TODO Auto-generated method stub
		
	}

	private void beforeExecute(Thread wt, Runnable task) {
		// TODO Auto-generated method stub
		
	}

	public MyThreadPoolExecutor(int corePoolSize,
            int maximumPoolSize,
            long keepAliveTime,
            TimeUnit unit,
            BlockingQueue<Runnable> workQueue,
            ThreadFactory threadFactory,
            RejectedExecutionHandler handler) {
		
        // 验证输入参数
    	if (corePoolSize < 0 ||
            maximumPoolSize <= 0 ||
            maximumPoolSize < corePoolSize ||
            keepAliveTime < 0)
            throw new IllegalArgumentException();
        if (workQueue == null || threadFactory == null || handler == null)
            throw new NullPointerException();
        this.acc = System.getSecurityManager() == null ?
                null :
                AccessController.getContext();
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
	}
	
	@Override
	public void execute(Runnable command) {
		// TODO Auto-generated method stub
		int c = ctl.get();
		
		if(workerCountOf(c) < corePoolSize) {
			if(addWorker(command, true)) {
				return;
			}
			c = ctl.get();
		}
		
		if(isRunning(c) && workQueue.offer(command)) {
			int recheck = ctl.get();
			if(!isRunning(recheck) && workQueue.remove(command)) {
				reject(command);
			}else if(workerCountOf(recheck) == 0) {
				addWorker(null, false);
			}
		} else if(!addWorker(command, false)){
			reject(command);
		}
	}
	
	
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Runnable> shutdownNow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isShutdown() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}



}

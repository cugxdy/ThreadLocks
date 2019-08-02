package io.thread.test;


import io.thread.pool.MyThreadPoolExecutor;

public interface MyRejectedExecutionHandler {
	void rejectedExecution(Runnable r, MyThreadPoolExecutor executor);
}

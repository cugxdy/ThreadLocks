package cugxdy.util.concurrent;

import cugxdy.util.concurrent.ExecutionException;
import cugxdy.util.concurrent.TimeUnit;
import cugxdy.util.concurrent.TimeoutException;

public interface Future<V> {
	
	// 它用来取消任务，如果取消任务成功则返回true，如果取消任务失败则返回false。
    boolean cancel(boolean mayInterruptIfRunning);
    // 它用来判断任务是否被取消成功，如果在任务正常完成前被取消成功，则返回 true。
    boolean isCancelled();
    // 它用来判断任务是否已经完成，若任务完成，则返回true；
    boolean isDone();
    // 它用来获取执行结果，这个方法会产生阻塞，会一直等到任务执行完毕才返回；
    V get() throws InterruptedException, ExecutionException;
    // 它用来获取执行结果，如果在指定时间内，还没获取到结果，就直接返回null。
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}

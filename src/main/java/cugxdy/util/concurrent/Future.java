package cugxdy.util.concurrent;

import cugxdy.util.concurrent.ExecutionException;
import cugxdy.util.concurrent.TimeUnit;
import cugxdy.util.concurrent.TimeoutException;

public interface Future<V> {
	
	// ������ȡ���������ȡ������ɹ��򷵻�true�����ȡ������ʧ���򷵻�false��
    boolean cancel(boolean mayInterruptIfRunning);
    // �������ж������Ƿ�ȡ���ɹ�������������������ǰ��ȡ���ɹ����򷵻� true��
    boolean isCancelled();
    // �������ж������Ƿ��Ѿ���ɣ���������ɣ��򷵻�true��
    boolean isDone();
    // ��������ȡִ�н������������������������һֱ�ȵ�����ִ����ϲŷ��أ�
    V get() throws InterruptedException, ExecutionException;
    // ��������ȡִ�н���������ָ��ʱ���ڣ���û��ȡ���������ֱ�ӷ���null��
    V get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException;
}

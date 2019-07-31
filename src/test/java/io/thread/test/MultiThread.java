package io.thread.test;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import org.junit.Test;

public class MultiThread {
	
	@Test
	public void TestThread() {
		
		ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
		
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
		for(ThreadInfo threadInfo : threadInfos) {
			System.out.println("["+threadInfo.getThreadId()+"] "+ threadInfo.getThreadName());
		}
		 
	}

}

package io.thread.atomic;

import org.junit.Test;

import cugxdy.util.concurrent.atomic.AtomicInteger;

public class AtomicTest {
	@Test
	public void test() {
		AtomicInteger id = new AtomicInteger(1);
		System.out.println(id.get() + "ppppppppppppppp");
		
	}
}

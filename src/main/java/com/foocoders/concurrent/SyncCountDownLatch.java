package com.foocoders.concurrent;

import java.util.concurrent.CountDownLatch;

public class SyncCountDownLatch implements Sync {
	
	private CountDownLatch countDownLatch = new CountDownLatch(1);
	
	public void await() throws InterruptedException{
		countDownLatch.await();
	}
	
	public void release() {
		countDownLatch.countDown();
	}
	

}

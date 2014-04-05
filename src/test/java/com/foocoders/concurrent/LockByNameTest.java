package com.foocoders.concurrent;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author pmoretti
 * 
 */
public class LockByNameTest {

	private LockByName<ReentrantLock> lockByName;

	private ScheduledExecutorService scheduledThreadPool;
	
	private ConcurrentLinkedQueue<Runnable> assertQueue;
	
	private String variable1;
	

	@Before
	public void setUp() throws Exception {

		lockByName = new LockByName<ReentrantLock>();

		scheduledThreadPool = Executors.newScheduledThreadPool(100);
		// warm up scheduledThreadPool
		scheduledThreadPool.schedule(new Runnable() {
			public void run() {

			}
		}, 0, TimeUnit.NANOSECONDS);
		
		variable1 = null;
		
		assertQueue = new ConcurrentLinkedQueue<Runnable>();

	}

	@After
	public void tearDown() throws Exception {
		
		if(!scheduledThreadPool.isTerminated()){
			scheduledThreadPool.awaitTermination(1, TimeUnit.SECONDS);
		}
		for (Runnable assertToTest : assertQueue) {
			System.out.println(assertToTest);
			assertToTest.run();
		}
		
		scheduledThreadPool.shutdown();
	}

	@Test
	public void testGetLock() throws InterruptedException {
		
		variable1 = "first-value";
		
		scheduledThreadPool.schedule(new Runnable() {
			public void run() {
				Lock reentrantLock1 = lockByName.getLock("variable1");
				try {
					reentrantLock1.lock();
					Thread.sleep(100);
					variable1 = "second-value";
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				} finally {
					reentrantLock1.unlock();
				}
			}
		}, 0, TimeUnit.MILLISECONDS);
		
		scheduledThreadPool.schedule(new Runnable() {
			public void run() {
				Lock reentrantLock1 = lockByName.getLock("variable1");
				try {
					reentrantLock1.lock();
					final String tempVariable1  = variable1;
					assertQueue.add(new Runnable() {
						public void run() {
							assertEquals("second-value", tempVariable1);
						}
					});
					
				} finally {
					reentrantLock1.unlock();
				}
			}
		}, 10, TimeUnit.MILLISECONDS);
		
	}

}

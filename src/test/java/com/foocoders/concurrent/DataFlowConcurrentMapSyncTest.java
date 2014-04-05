package com.foocoders.concurrent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DataFlowConcurrentMapSyncTest {

	private DataFlowConcurrentMapSync<String> dataflow;

	private ScheduledExecutorService scheduledThreadPool;

	private ConcurrentLinkedQueue<Runnable> assertQueue;

	@Before
	public void setUp() throws Exception {

		dataflow = new DataFlowConcurrentMapSync<String>();

		scheduledThreadPool = Executors.newScheduledThreadPool(100);
		// warm up scheduledThreadPool
		scheduledThreadPool.schedule(new Runnable() {
			public void run() {

			}
		}, 0, TimeUnit.NANOSECONDS);

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

	static public void assertRate(long expected, int rate, long actual) {
		long diff = ((expected * rate) / 100);
		long min = expected - diff;
		long max = expected + diff;
		if (!(actual > min && actual < max)) {
			fail("asserRate expected:" + expected + " actual:" + actual + " rate:" + rate);
		}
	}

	@Test
	public void testSetAndGetWithOneThread() throws InterruptedException {
		dataflow.set("number1", "1");
		dataflow.set("number2", "2");
		assertEquals(dataflow.get("number1"), "1");
		assertEquals(dataflow.get("number2"), "2");
	}

	@Test
	public void testGetAndSet() throws InterruptedException {

		scheduledThreadPool.schedule(new DataFlowTask<String>(dataflow) {
			@Override
			public void run(DataFlow<String> dataflow) {
				dataflow.set("number1", "1");
			}
		}, 10, TimeUnit.MILLISECONDS);

		assertEquals(dataflow.get("number1"), "1");
	}

	@Test
	public void testGetAndSetAndGetConccurrent() throws InterruptedException {

		scheduledThreadPool.schedule(new DataFlowTask<String>(dataflow) {
			@Override
			public void run(DataFlow<String> dataflow) {
				dataflow.set("number1", "1");
				try {

					final String temNumber1 = dataflow.get("number1");

					assertQueue.add(new Runnable() {
						public void run() {
							assertEquals(temNumber1, "1");

						}
						
					});

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}, 10, TimeUnit.MILLISECONDS);

		assertEquals(dataflow.get("number1"), "1");
		

	}

	@Test
	public void testExpireGetByTimeout() throws InterruptedException {

		long time = System.currentTimeMillis();
		dataflow.get("number1", 100, TimeUnit.MILLISECONDS);
		assertRate(100, 10, System.currentTimeMillis() - time);

	}

}

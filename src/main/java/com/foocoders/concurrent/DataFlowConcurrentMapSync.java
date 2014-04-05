package com.foocoders.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataFlowConcurrentMapSync<T> implements DataFlow<T> {

	private ConcurrentHashMap<String, DataflowVariable<T>> localMap = new ConcurrentHashMap<String, DataflowVariable<T>>();

	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1);

	private static Sync SYNC_DUMMY = new Sync() {
		public void await() throws InterruptedException {
		}

		public void release() {
		}
	};

	public void shutdownNow(long time, TimeUnit unit) {
		scheduledThreadPool.schedule(new Runnable() {
			public void run() {
				shutdownNow();
			}
		}, time, unit);
	}

	public void shutdownNow() {
		scheduledThreadPool.shutdownNow();
	}

	public void releaseAllKeys() {
		for (String key : localMap.keySet()) {
			set(key, null);
		}
	}

	public T get(String key, long milliseconds) throws InterruptedException {
		return get(key, milliseconds, TimeUnit.MILLISECONDS);
	}

	public T get(String key, long time, TimeUnit unit) throws InterruptedException {
		scheduledThreadPool.schedule(new ReleaseKey<T>(this, key), time, unit);
		return get(key);
	}

	public T get(String key) throws InterruptedException {

		DataflowVariable<T> dataflowVariable = localMap.get(key);

		if (dataflowVariable == null) {

			DataflowVariable<T> newDataflowVariable = new DataflowVariable<T>(getSyncImplementation());

			dataflowVariable = localMap.putIfAbsent(key, newDataflowVariable);

			if (dataflowVariable == null) {
				dataflowVariable = newDataflowVariable;
			}
		}

		return dataflowVariable.getVariable();

	}

	public void set(String key, T variable) {

		DataflowVariable<T> newDataflowVariable = new DataflowVariable<T>(variable, SYNC_DUMMY);

		DataflowVariable<T> dataflowVariable = localMap.putIfAbsent(key, newDataflowVariable);

		if (dataflowVariable != null) {
			dataflowVariable.setVariable(variable);
		}

	}

	protected Sync getSyncImplementation() {
		return new SyncCountDownLatch();
	}

	static private class DataflowVariable<T> {

		private T variable;
		private Sync sync;

		DataflowVariable(Sync sync) {
			this.sync = sync;
		}

		DataflowVariable(T variable, Sync sync) {
			this.variable = variable;
			this.sync = sync;
		}

		public void setVariable(T variable) {
			this.variable = variable;
			sync.release();
		}

		public T getVariable() throws InterruptedException {
			sync.await();
			return variable;
		}

	}

	static public class ReleaseAllKeys<T> implements Runnable {

		private DataFlow<T> dataflow;

		public ReleaseAllKeys(DataFlow<T> dataflow) {
			this.dataflow = dataflow;
		}

		public void run() {
			dataflow.releaseAllKeys();
		}

	}

	static public class ReleaseKey<T> implements Runnable {

		private DataFlow<T> dataflow;
		private String key;

		public ReleaseKey(DataFlow<T> dataflow, String key) {
			this.dataflow = dataflow;
			this.key = key;
		}

		public void run() {
			dataflow.set(key, null);
		}

	}

}

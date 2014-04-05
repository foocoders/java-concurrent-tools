package com.foocoders.concurrent;

abstract public class DataFlowTask<T> implements Runnable {
	
	private DataFlow<T> dataflow;

	public DataFlowTask( DataFlow<T> dataflow) {
		this.dataflow = dataflow;
	}
	
	final public void run() {
		run(dataflow);
	}

	abstract public void run( DataFlow<T> dataflow);
	
}

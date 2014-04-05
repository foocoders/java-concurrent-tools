package com.foocoders.concurrent;

public interface Sync {
	
	public void await() throws InterruptedException;
	
	public void release();

}
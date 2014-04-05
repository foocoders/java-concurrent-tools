package com.foocoders.concurrent;

import java.util.concurrent.TimeUnit;

public interface DataFlow<T> {

	
	public T get(String key) throws InterruptedException;
	
	public T get(String key,long time, TimeUnit unit) throws InterruptedException;
	
	public T get(String key, long milliseconds) throws InterruptedException;

	public void set(String key,T value);

	public void releaseAllKeys();

}

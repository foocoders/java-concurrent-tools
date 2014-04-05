package com.foocoders.concurrent;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
 
public class LockByName<T extends Lock> {
 
	ConcurrentHashMap<String, Lock> mapStringLock;
	
	public LockByName(){
		mapStringLock = new ConcurrentHashMap<String, Lock>();
	}
	
	public LockByName(ConcurrentHashMap<String, Lock> mapStringLock){
		this.mapStringLock = mapStringLock;
	}
 
	public Lock getLock(String key) {
		Lock initValue = createIntanceLock();
		Lock lock = mapStringLock.putIfAbsent(key, initValue);
		if (lock == null) {
			lock = initValue;
		}
		return lock;
	}
 
	protected Lock createIntanceLock() {
		return new ReentrantLock();
	}
	
}
package net.vicp.lylab.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import net.vicp.lylab.core.exception.LYException;
import net.vicp.lylab.utils.tq.Task;

public class BaseObject {

	protected transient long objectId = 0L;

	public long getObjectId() {
		return objectId;
	}

	public BaseObject setObjectId(long objectId) {
		this.objectId = objectId;
		return this;
	}
	
	/**
	 * Now every BaseObject may use this to log something
	 */
	protected static Log log = LogFactory.getLog(Task.class);

	protected static class Lock { };
	protected Lock lock = new Lock();

	protected void await(long timeout)
	{
		synchronized (lock) {
			try {
				lock.wait(timeout);
			} catch (Exception e) {
				throw new LYException("Waiting Interrupted");
			}
		}
	}

	protected void await()
	{
		synchronized (lock) {
			try {
				lock.wait();
			} catch (Exception e) {
				throw new LYException("Waiting Interrupted");
			}
		}
	}
	
	protected void signal()
	{
		synchronized (lock) {
			lock.notify();
		}
	}
	
	protected void signalAll()
	{
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
}

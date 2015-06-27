package net.vicp.lylab.utils.atomic;

public final class AtomicInteger extends AtomicObject<Integer> {

	public AtomicInteger() {
		super(0);
	}
	
	public AtomicInteger(int t) {
		super(t);
	}
	
    /**
     * Gets the current read-only value.
     *
     * @return the current value
     */
	@Override
	public Integer get()
	{
		return value.intValue();
	}

    /**
     * Atomically adds the given value to the current value.
     *
     * @param delta the value to add
     * @return the previous value
     */
	public Integer getAndAdd(int delta) {
		synchronized (lock) {
			int current = get();
			value += delta;
			return current;
		}
	}

	/**
	 * Atomically decrements by one the current value.
	 *
	 * @return the updated value
	 */
	public Integer decrementAndGet() {
		synchronized (lock) {
			value--;
			return value;
		}
	}
	
	/**
	 * Atomically increments by one the current value.
	 *
	 * @return the updated value
	 */
	public Integer incrementAndGet() {
		synchronized (lock) {
			value++;
			return value;
		}
	}
	
}

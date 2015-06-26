package net.vicp.lylab.utils.atomic;

import java.lang.ref.SoftReference;

public final class AtomicSoftReference<T> extends AtomicReference<T> {

	public AtomicSoftReference() {
		this(null);
	}

	public AtomicSoftReference(SoftReference<T> ref) {
		super(ref);
	}

	public T get(Class<T> instanceClass)
	{
		if(value == null) createInstance(instanceClass, SoftReference.class);
		return value.get();
	}
	

}

package net.vicp.lylab.core.pool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.vicp.lylab.core.CoreDefine;

/**
 * 
 * @author liyang
 *
 * 顺序化数据池
 *
 */
public class SequenceTemporaryPool<T> extends SequencePool<T> {

	public SequenceTemporaryPool() {
		this(CoreDefine.DEFAULT_MAX_SIZE);
	}

	public SequenceTemporaryPool(int maxSize) {
		super(maxSize);
	}
	
	@Override
	public T accessOne() {
		synchronized (lock) {
			safeCheck();
			if (keyContainer.isEmpty())
				return null;
			T tmp = null;
			Long key = keyContainer.get(0);
			tmp = removeFromContainer(key);
			keyContainer.remove(0);
			return tmp;
		}
	}

	@Override
	public List<T> accessMany(int amount) {
		synchronized (lock) {
			safeCheck();
			List<T> retList = new ArrayList<T>();
			Iterator<Long> iterator = keyContainer.iterator();
			for (int i = 0; !iterator.hasNext() && i < amount; i++) {
				retList.add(removeFromContainer(iterator.next()));
				iterator.remove();
			}
			return retList;
		}
	}

}

package com.game.common.util;

import java.util.Iterator;

import com.game.common.model.Entity;
import com.game.common.model.Hash;

public class EntityListIterator<T extends Entity> implements Iterator<T> {

	protected final EntityList<T> list;
	protected final Hash[] keySet;
	protected Hash current;
	protected int index;

	public EntityListIterator(EntityList<T> list) {
		this.list = list;

		keySet = list.keySet().toArray(new Hash[list.size()]);
		current = null;
		index = 0;
	}

	@Override
	public boolean hasNext() {
		return index < keySet.length;
	}

	@Override
	public T next() {
		current = keySet[index++];
		return list.get(current);
	}

	@Override
	public void remove() {
		if (current == null)
			return;

		list.remove(current);
	}
}

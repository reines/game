package com.game.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Entity;
import com.game.common.model.Hash;

public class EntityList<T extends Entity> implements Observer, Iterable<T> {
	private static final Logger log = LoggerFactory.getLogger(EntityList.class);

	protected final Map<Hash, T> allEntities;
	protected final Set<T> newEntities;
	protected final Set<T> removedEntities;
	protected final Set<T> updatedEntities;

	public EntityList() {
		allEntities = new HashMap<Hash, T>();
		newEntities = new HashSet<T>();
		removedEntities = new HashSet<T>();
		updatedEntities = new HashSet<T>();
	}

	public boolean add(T entity) {
		if (allEntities.containsKey(entity.getID()))
			return false;

		allEntities.put(entity.getID(), entity);
		newEntities.add(entity);

		entity.addObserver(this);

		if (log.isDebugEnabled())
			log.debug("Added entity: " + entity);

		return true;
	}

	public Set<Hash> keySet() {
		return allEntities.keySet();
	}

	public boolean contains(T entity) {
		return allEntities.containsKey(entity.getID());
	}

	public T get(Hash id) {
		return allEntities.get(id);
	}

	public T remove(Hash id) {
		T entity = allEntities.remove(id);
		if (entity == null)
			return null;

		entity.deleteObserver(this);

		newEntities.remove(entity);
		removedEntities.add(entity);

		if (log.isDebugEnabled())
			log.debug("Removed entity: " + entity);

		return entity;
	}

	public int size() {
		return allEntities.size();
	}

	public Collection<T> allEntities() {
		return allEntities.values();
	}

	public boolean hasNewEntities() {
		return !newEntities.isEmpty();
	}

	public Collection<T> newEntities() {
		return newEntities;
	}

	public boolean hasRemovedEntities() {
		return !removedEntities.isEmpty();
	}

	public Collection<T> removedEntities() {
		return removedEntities;
	}

	public boolean hasUpdatedEntities() {
		return !updatedEntities.isEmpty();
	}

	public Collection<T> updatedEntities() {
		return updatedEntities;
	}

	public void reset() {
		newEntities.clear();
		removedEntities.clear();
		updatedEntities.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void update(Observable o, Object arg) {
		updatedEntities.add((T) o);
	}

	@Override
	public Iterator<T> iterator() {
		return new EntityListIterator<T>(this);
	}
}

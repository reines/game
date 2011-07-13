package com.game.common.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Inventory implements Iterable<Item>, Serializable {
	private static final long serialVersionUID = 1L;

	public static final int MAX_SIZE = 40;

	protected List<Item> items;

	public Inventory() {
		items = new ArrayList<Item>();
	}

	public int add(Item item) {
		// TODO: Handle stackable items properly
		synchronized (items) {
			if (items.size() >= MAX_SIZE)
				return -1;

			items.add(item);
			return items.size() - 1;
		}
	}

	public Item get(int index) {
		if (index >= items.size())
			return null;

		return items.get(index);
	}

	public Item remove(int index) {
		if (index >= items.size())
			return null;

		return items.remove(index);
	}

	public int size() {
		return items.size();
	}

	@Override
	public String toString() {
		return "inventory[size = " + items.size() + "]";
	}

	@Override
	public Iterator<Item> iterator() {
		return items.iterator();
	}
}

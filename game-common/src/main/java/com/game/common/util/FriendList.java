package com.game.common.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.game.common.model.Friend;
import com.game.common.model.Hash;

public class FriendList implements Iterable<Friend>, Serializable {
	private static final long serialVersionUID = 1L;

	protected Map<Hash, Friend> friends;

	public FriendList() {
		friends = new HashMap<Hash, Friend>();
	}

	public void add(Friend friend) {
		friends.put(friend.getID(), friend);
	}

	public boolean contains(Hash id) {
		return friends.containsKey(id);
	}

	public Friend get(Hash id) {
		return friends.get(id);
	}

	public Friend remove(Hash id) {
		return friends.remove(id);
	}

	public int size() {
		return friends.size();
	}

	@Override
	public Iterator<Friend> iterator() {
		return friends.values().iterator();
	}
}

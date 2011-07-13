package com.game.common.model;

import java.io.Serializable;
import java.util.Date;

import com.game.common.util.FriendList;
import com.game.common.util.StatList;

public class PlayerProfile implements Serializable {
	private static final long serialVersionUID = 1L;

	public Hash id;

	@SuppressWarnings("unused")
	private Hash password; // for hibernate

	public String username;
	public Point location;

	public Inventory inventory;
	public StatList stats;
	public FriendList friends;

	public Date registered;
	public Date lastSession;

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PlayerProfile))
			return false;

		PlayerProfile p = (PlayerProfile) o;
		return id.equals(p.id);
	}

	@Override
	public String toString() {
		return "profile[id = " + id + ", username = '" + username + "', location = " + location + "]";
	}
}

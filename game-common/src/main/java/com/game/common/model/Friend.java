package com.game.common.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Friend implements Serializable {
	private static final long serialVersionUID = 1L;

	protected Hash id;
	protected String username;
	protected transient boolean online;

	public Friend(Hash id, String username, boolean online) {
		this.id = id;
		this.username = username;
		this.online = online;
	}

	@SuppressWarnings("unused")
	private Friend() { } // for hibernate

	public Hash getID() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Friend))
			return false;

		Friend f = (Friend) o;
		return id.equals(f.id);
	}

	@Override
	public String toString() {
		return "friend[id = " + id + ", username = '" + username + "', online = " + online + "]";
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.setOnline(false); // Default to offline unless we're told otherwise
	}
}

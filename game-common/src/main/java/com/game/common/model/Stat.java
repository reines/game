package com.game.common.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Observable;

public class Stat extends Observable implements Comparable<Stat>, Serializable {
	private static final long serialVersionUID = 1L;

	public static int expToLevel(long exp) {
		return 1; // TODO: calculate the level from the experience
	}

	public static long levelToExp(int level) {
		// NOTE: first ensure level is within bounds, i.e. if level > MAX_LEVEL: level = MAX_LEVEL
		return 0; // TODO: calculate the experience required for the level
	}

	public enum Type {
		ATTACK("Attack"),
		DEFENSE("Defense"),
		STRENGTH("Strength"),
		HITPOINTS("Hitpoints"),
		MAGIC("Magic"),
		ARCHERY("Archery");

		public final String name;

		private Type(String name) {
			this.name = name;
		}
	};

	protected Type type;
	protected long exp;
	protected transient int level;
	protected int current;

	@SuppressWarnings("unused")
	private Stat() { } // for hibernate

	public Stat(Type type, long exp, int current) {
		this.type = type;
		this.current = current;
		this.setExp(exp);
	}

	public void setType(Stat.Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	public void setExp(long exp) {
		this.exp = exp;
		this.level = Stat.expToLevel(exp);

		super.setChanged();
		super.notifyObservers(false);
	}

	public long getExp() {
		return exp;
	}

	public void setCurrent(int current) {
		this.current = current;

		super.setChanged();
		super.notifyObservers(false);
	}

	public int getCurrent() {
		return current;
	}

	public int getLevel() {
		return level;
	}

	public boolean incExp(long exp) {
		int level = this.level;
		this.setExp(this.exp + exp);

		if (this.level != level) {
			super.setChanged();
			super.notifyObservers(true);
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return type.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Stat))
			return false;

		Stat s = (Stat) o;
		return exp == s.exp && current == s.current;
	}

	@Override
	public int compareTo(Stat s) {
		// compares based on the current level, not the experience!
		return current - s.current;
	}

	@Override
	public String toString() {
		return current + "/" + level;
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.setExp(exp); // Re-set the experience, this will calculate the level for us
	}
}

package com.game.common.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import com.game.common.model.Stat;

public class StatList implements Iterable<Stat>, Observer, Serializable {
	private static final long serialVersionUID = 1L;

	public static final int NUM_STATS;

	static {
		NUM_STATS = Stat.Type.values().length;
	}

	protected List<Stat> stats;
	protected transient int combatLevel;
	protected transient int skillTotal;

	public StatList(List<Stat> stats) {
		// Add us as an observer for all the stats
		for (Stat stat : stats)
			stat.addObserver(this);

		this.stats = stats;

		// Calculate the combat level and skill total
		this.update(null, true);
	}

	@SuppressWarnings("unused")
	private StatList() { } // for hibernate

	public Stat get(Stat.Type type) {
		return stats.get(type.ordinal());
	}

	public synchronized int getCombatLevel() {
		return combatLevel;
	}

	public synchronized int getSkillTotal() {
		return skillTotal;
	}

	public int size() {
		return stats.size();
	}

	@Override
	public Iterator<Stat> iterator() {
		return stats.iterator();
	}

	protected synchronized int calcCombatLevel() {
		int total = 0;

		total += this.get(Stat.Type.ATTACK).getLevel();
		total += this.get(Stat.Type.DEFENSE).getLevel();
		total += this.get(Stat.Type.STRENGTH).getLevel();
		total += this.get(Stat.Type.MAGIC).getLevel();
		total += this.get(Stat.Type.ARCHERY).getLevel();

		total /= 2;

		total += this.get(Stat.Type.HITPOINTS).getLevel();

		return total / 3;
	}

	protected synchronized int calcSkillTotal() {
		int total = 0;

		for (Stat stat : this)
			total += stat.getLevel();

		return total;
	}

	@Override
	public synchronized void update(Observable o, Object arg) {
		boolean changedLevel = (Boolean) arg;
		// Check if the level changed, or just the experience
		if (!changedLevel)
			return;

		// Something has changed, update the combat level and skill total
		combatLevel = this.calcCombatLevel();
		skillTotal = this.calcSkillTotal();
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.update(null, true); // Call a fake update to calculate the combat level and skill total
	}
}

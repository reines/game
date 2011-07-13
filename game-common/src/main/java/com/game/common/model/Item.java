package com.game.common.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.util.PersistenceManager;

public class Item implements Serializable {
	private static final Logger log = LoggerFactory.getLogger(Item.class);
	private static final long serialVersionUID = 1L;

	protected static final Map<Integer, Definition> definitions;

	static {
		definitions = Item.loadDefinitions();

		if (log.isDebugEnabled())
			log.debug("Loaded " + definitions.size() + " item definitions");
	}

	public static Map<Integer, Definition> getDefinitions() {
		return definitions;
	}

	// do nothing - static {} does the work
	public static void load() { }

	@SuppressWarnings("unchecked")
	private static Map<Integer, Definition> loadDefinitions() {
		URL path = Item.class.getResource("items.xml");
		if (path == null) {
			log.error("Unable to find items.xml resource");
			System.exit(1); // fatal error
			return null;
		}

		return (Map<Integer, Definition>) PersistenceManager.load(path);
	}

	public static class Definition {
		public static final Definition DEFAULT_DEFINITION;

		static {
			DEFAULT_DEFINITION = new Definition();
			DEFAULT_DEFINITION.name = "Unknown Item";
			DEFAULT_DEFINITION.stackable = false;
			DEFAULT_DEFINITION.equipable = Equipable.NOT_EQUIPABLE;
			DEFAULT_DEFINITION.edible = 0;
			DEFAULT_DEFINITION.description = "An unknown item.";
		}

		public enum Equipable {
			NOT_EQUIPABLE,
			HEAD,
		};

		public String name;
		public boolean stackable;
		public Equipable equipable;
		public byte edible;
		public String description;
	}

	protected int id;
	protected long amount;
	protected boolean equiped;
	protected transient Definition definition;

	public Item(int id) {
		this (id, 1, false);
	}

	public Item(int id, long amount, boolean equiped) {
		this.setID(id);
		this.setAmount(amount);
		this.setEquiped(equiped);
	}

	@SuppressWarnings("unused")
	private Item() { } // for hibernate

	// for hibernate
	private void setID(int id) {
		this.id = id;

		definition = definitions.get(id);
		if (definition == null)
			definition = Definition.DEFAULT_DEFINITION;
	}

	public int getID() {
		return id;
	}

	public long getAmount() {
		return amount;
	}

	public void setAmount(long amount) {
		// If the item isn't stackable it's amount should always be 1
		if (amount != 1 && !this.isStackable()) {
			log.error("Attempted to set amount of non-stackable item.");
			return;
		}

		this.amount = amount;
	}

	public boolean isEquiped() {
		return equiped;
	}

	public void setEquiped(boolean equiped) {
		// If the item isn't equipable it should never be equiped
		if (equiped && !this.isEquipable()) {
			log.error("Attempted to equip non-equipable item.");
			return;
		}

		this.equiped = equiped;
	}

	public boolean isEquipable() {
		return definition.equipable != Definition.Equipable.NOT_EQUIPABLE;
	}

	public Definition.Equipable getEquipable() {
		return definition.equipable;
	}

	public boolean isEdible() {
		return definition.edible > 0;
	}

	public int getEdible() {
		return definition.edible;
	}

	public String getName() {
		return definition.name;
	}

	public String getDescription() {
		return definition.description;
	}

	public boolean isStackable() {
		return definition.stackable;
	}

	@Override
	public String toString() {
		return "item[id = " + id + ", name = '" + definition.name + "', amount = " + amount + ", equiped = " + equiped + "]";
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
		this.setID(id); // Re-set the ID, this will set our definition up
	}
}

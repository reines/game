package com.game.common.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.util.PersistenceManager;

public abstract class Tile {
	private static final Logger log = LoggerFactory.getLogger(Tile.class);

	public static final int DATA_LENGTH = 5;

	protected static final Map<Byte, Definition> definitions;

	static {
		definitions = Tile.loadDefinitions();

		if (log.isDebugEnabled())
			log.debug("Loaded " + definitions.size() + " tile definitions");
	}

	public static Map<Byte, Definition> getDefinitions() {
		return definitions;
	}

	// do nothing - static {} does the work
	public static void load() { }

	@SuppressWarnings("unchecked")
	private static Map<Byte, Definition> loadDefinitions() {
		URL path = Tile.class.getResource("tiles.xml");
		if (path == null) {
			log.error("Unable to find tiles.xml resource");
			System.exit(1); // fatal error
			return null;
		}

		return (Map<Byte, Definition>) PersistenceManager.load(path);
	}

	public static class Definition {
		public static final Definition DEFAULT_DEFINITION;

		static {
			DEFAULT_DEFINITION = new Definition();
			DEFAULT_DEFINITION.walkable = false;
		}

		public boolean walkable;
	}

	protected byte elevation;
	protected byte texture;
	protected byte overlay;

	protected byte hWall;
	protected byte vWall;

	protected Definition definition;

	public Tile(DataInputStream in) throws IOException {
		elevation = in.readByte();
		texture = in.readByte();
		overlay = in.readByte();

		hWall = in.readByte();
		vWall = in.readByte();

		definition = definitions.get(texture);
		if (definition == null)
			definition = Definition.DEFAULT_DEFINITION;
	}

	protected Tile() {
		elevation = 0;
		texture = 0;
		overlay = 0;

		hWall = 0;
		vWall = 0;

		definition = definitions.get(texture);
		if (definition == null)
			definition = Definition.DEFAULT_DEFINITION;
	}

	public boolean isWalkable() {
		return definition.walkable;
	}

	public boolean isBlockedHorizontally() {
		// If there is any kind of wall, we are blocked
		return hWall > 0 || !this.isWalkable();
	}

	public boolean isBlockedVertically() {
		// If there is any kind of wall, we are blocked
		return vWall > 0 || !this.isWalkable();
	}

	public int getElevation() {
		return elevation;
	}

	public int getTexture() {
		return texture;
	}

	public boolean hasOverlay() {
		return overlay > 0;
	}

	public int getOverlay() {
		return overlay - 1;
	}

	public boolean hasHWall() {
		return hWall > 0;
	}

	public int getHWall() {
		return hWall - 1;
	}

	public boolean hasVWall() {
		return vWall > 0;
	}

	public int getVWall() {
		return vWall - 1;
	}
}

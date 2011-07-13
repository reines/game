package com.game.server.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.model.Tile;

public class WorldTile extends Tile {
	private static final Logger log = LoggerFactory.getLogger(WorldTile.class);

	protected Set<Player> players; // TODO: Is there actually any need to know what players are on a tile?

	public WorldTile(DataInputStream in) throws IOException {
		super (in);

		players = null;
	}

	public void add(Player player) {
		// If there are no clients here yet the client list will be null to save memory
		if (players == null)
			players = new HashSet<Player>();

		players.add(player);

		if (!super.isWalkable())
			log.warn("Added player to a non-walkable tile: " + player);
	}

	public boolean contains(Player player) {
		if (player == null)
			return false;

		return players.contains(player);
	}

	public void remove(Player player) {
		if (players == null)
			return;

		players.remove(player);

		// If this tile is now empty, destroy the set to save memory
		if (players.isEmpty())
			players = null;
	}
}

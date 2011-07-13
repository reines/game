package com.game.client.handlers.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.handlers.PacketHandler;
import com.game.client.model.Player;
import com.game.common.codec.Packet;
import com.game.common.model.Hash;
import com.game.common.model.Point;

public class PlayerHandler implements PacketHandler {
	private static final Logger log = LoggerFactory.getLogger(PlayerHandler.class);

	@Override
	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception {
		switch (packet.getType()) {
		// Add new players
		case PLAYERS_ADD_RESPONSE: {
			int playerCount = packet.getShort();
			for (int i = 0;i < playerCount;i++) {
				Hash id = packet.getHash();
				String username = packet.getString();
				Point location = packet.getPoint();

				Player player = new Player(id, username, location, null); // TODO: The players model?
				world.addPlayer(player);

				if (log.isDebugEnabled())
					log.debug("Added new player: " + player);
			}

			break;
		}

		// Remove existing players
		case PLAYERS_REMOVE_RESPONSE: {
			int playerCount = packet.getShort();
			for (int i = 0;i < playerCount;i++) {
				Player player = world.removePlayer(packet.getHash());

				if (log.isDebugEnabled())
					log.debug("Removed old player: " + player);
			}

			break;
		}

		// Update existing players
		case PLAYERS_UPDATE_RESPONSE: {
			int playerCount = packet.getShort();
			for (int i = 0;i < playerCount;i++) {
				Hash id = packet.getHash();
				Point location = packet.getPoint();

				// If it's an update for ourself
				if (id.equals(world.getID())) {
					world.setLocation(location);
					continue;
				}

				Player player = world.getPlayer(id);
				// It's an update for a player we don't yet know about
				if (player == null)
					continue;

				player.setLocation(location);
			}

			break;
		}
		}
	}
}

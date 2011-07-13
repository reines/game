package com.game.server.handlers.packet;

import com.game.common.codec.Packet;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.PacketHandler;
import com.game.server.model.Player;

public class ChatHandler implements PacketHandler {
	public static final int CHAT_RADIUS = 32;

	@Override
	public void handlePacket(Server server, WorldManager world, Player player, Packet packet) throws Exception {
		String message = packet.getString();

		// Send to all players that we know about within the set radius
		// this should be the same as all players that know about us, within the set radius
		for (Player p : player.getKnownPlayers()) {
			if (p.equals(player) || player.getLocation().distanceTo(p.getLocation()) > CHAT_RADIUS)
				continue;

			p.sendChat(player, message);
		}
	}
}

package com.game.server.handlers.packet;

import com.game.common.codec.Packet;
import com.game.common.model.Path;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.PacketHandler;
import com.game.server.model.Player;

public class WalkHandler implements PacketHandler {

	@Override
	public void handlePacket(Server server, WorldManager world, Player player, Packet packet) throws Exception {
		Path path = packet.getPath();
		// Check the path actually has some steps
		if (!path.hasNext())
			return;

		// Set this player walking along the path
		player.setPath(path);
	}
}

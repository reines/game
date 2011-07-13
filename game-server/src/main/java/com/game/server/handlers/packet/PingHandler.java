package com.game.server.handlers.packet;

import com.game.common.codec.Packet;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.PacketHandler;
import com.game.server.model.Player;

public class PingHandler implements PacketHandler {

	@Override
	public void handlePacket(Server server, WorldManager world, Player player, Packet packet) throws Exception {
		// Do nothing
	}
}

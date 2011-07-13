package com.game.server.handlers;

import com.game.common.codec.Packet;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.model.Player;

public interface PacketHandler {

	public void handlePacket(Server server, WorldManager world, Player player, Packet packet) throws Exception;

}

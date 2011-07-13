package com.game.client.handlers.packet;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.handlers.PacketHandler;
import com.game.common.codec.Packet;
import com.game.common.model.Stat;

public class StatHandler implements PacketHandler {

	@Override
	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception {
		Stat stat = world.getStats().get(packet.getEnum(Stat.Type.class));
		// Update the total experience (and hence level)
		stat.setExp(packet.getLong());
		// Update the current level
		stat.setCurrent(packet.getShort());
	}
}

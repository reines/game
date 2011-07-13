package com.game.client.handlers;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.common.codec.Packet;

public interface PacketHandler {

	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception;

}

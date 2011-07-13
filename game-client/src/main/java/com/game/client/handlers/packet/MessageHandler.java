package com.game.client.handlers.packet;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.handlers.PacketHandler;
import com.game.client.ui.ChatMessages;
import com.game.common.codec.Packet;

public class MessageHandler implements PacketHandler {

	@Override
	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception {
		String message = packet.getString();

		// Display the message
		client.getHUD().addMessage(message, ChatMessages.Type.MESSAGE);
	}
}

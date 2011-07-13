package com.game.client.handlers.packet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.handlers.PacketHandler;
import com.game.client.model.Player;
import com.game.client.ui.ChatMessages;
import com.game.common.codec.Packet;
import com.game.common.model.Hash;

public class ChatHandler implements PacketHandler {
	private static final Logger log = LoggerFactory.getLogger(ChatHandler.class);

	@Override
	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception {
		Hash id = packet.getHash();
		Player from = world.getPlayer(id);
		if (from == null) {
			log.warn("Received chat message from unknown player: " + id);
			return;
		}

		String message = packet.getString();

		// Send the message to our HUD to be displayed
		client.getHUD().addMessage(from.getUsername() + ": " + message, ChatMessages.Type.LOCAL_CHAT);

		// TODO: Display the chat message over the players head
	}
}

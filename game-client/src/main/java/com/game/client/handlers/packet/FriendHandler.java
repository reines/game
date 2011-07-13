package com.game.client.handlers.packet;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.handlers.PacketHandler;
import com.game.client.ui.ChatMessages;
import com.game.client.ui.HUD;
import com.game.common.codec.Packet;
import com.game.common.model.Friend;

public class FriendHandler implements PacketHandler {

	@Override
	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception {
		HUD hud = client.getHUD();

		switch (packet.getType()) {
		// Add a new friend
		case FRIEND_ADD_RESPONSE: {
			Friend friend = packet.getFriend();
			world.getFriends().add(friend);

			// Alert the user
			hud.addMessage(friend.getUsername() + " has been added to your friend list.", ChatMessages.Type.PRIVATE_CHAT);

			break;
		}

		// Delete a friend
		case FRIEND_REMOVE_RESPONSE: {
			Friend friend = world.getFriends().remove(packet.getHash());
			if (friend == null)
				return;

			// Alert the user
			hud.addMessage(friend.getUsername() + " has been removed from your friend list.", ChatMessages.Type.PRIVATE_CHAT);

			break;
		}

		// Received a message from a friend
		case FRIEND_MESSAGE_RESPONSE: {
			String username = packet.getString();
			String message = packet.getString();

			hud.addMessage(username + ": " + message, ChatMessages.Type.PRIVATE_CHAT);
			break;
		}

		// Update the friends online status
		case FRIEND_LOGIN_RESPONSE: {
			Friend friend = world.getFriends().get(packet.getHash());
			if (friend == null)
				return;

			boolean in = packet.getBoolean();
			friend.setOnline(in);

			// Alert the user
			hud.addMessage(friend.getUsername() + " has logged " + (in ? "in" : "out") + ".", ChatMessages.Type.PRIVATE_CHAT);

			break;
		}
		}
	}
}

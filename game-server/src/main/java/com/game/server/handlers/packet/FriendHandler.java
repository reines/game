package com.game.server.handlers.packet;

import com.game.common.codec.Packet;
import com.game.common.model.Friend;
import com.game.common.model.Hash;
import com.game.common.util.FriendList;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.PacketHandler;
import com.game.server.model.Player;

public class FriendHandler implements PacketHandler {

	@Override
	public void handlePacket(Server server, WorldManager world, Player player, Packet packet) throws Exception {
		FriendList friendList = player.getFriends();

		switch (packet.getType()) {
		// Add a new friend
		case FRIEND_ADD_SEND: {
			String username = packet.getString();

			// Look up the user from the database
			Friend friend = server.getDatabase().getFriend(username);
			if (friend == null) {
				player.sendMessage("Unable to add " + username + ", no such player found.");
				return;
			}

			// Add the friend
			friendList.add(friend);

			// Tell the client to actually perform the add
			player.sendAddFriend(friend);

			break;
		}

		// Delete a friend
		case FRIEND_REMOVE_SEND: {
			Hash id = packet.getHash();
			Friend friend = friendList.remove(id);
			if (friend == null)
				return;

			// Tell the client to actually perform the removal
			player.sendRemoveFriend(friend);

			break;
		}

		// Send a message to a friend
		case FRIEND_MESSAGE_SEND: {
			Friend friend = player.getFriends().get(packet.getHash());
			if (friend == null)
				return;

			String message = packet.getString();
			Player recipient = world.getPlayer(friend.getID());
			// Player is offline
			if (recipient == null) {
				player.sendMessage("Message not delivered, " + friend.getUsername() + " appears to be offline.");
				return;
			}

			// Send the message to the recipient
			recipient.sendFriendMessage(player.getUsername(), message);

			break;
		}
		}
	}
}

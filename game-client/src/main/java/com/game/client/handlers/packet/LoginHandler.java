package com.game.client.handlers.packet;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.client.Client;
import com.game.client.WorldManager;
import com.game.client.handlers.PacketHandler;
import com.game.client.ui.ChatMessages;
import com.game.common.codec.Packet;
import com.game.common.model.Inventory;
import com.game.common.model.PlayerProfile;
import com.game.common.model.Stat;
import com.game.common.util.FriendList;
import com.game.common.util.StatList;

public class LoginHandler implements PacketHandler {
	private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);

	@Override
	public void handlePacket(Client client, WorldManager world, Packet packet) throws Exception {
		try {
			boolean success = packet.getBoolean();
			log.debug("Received login response: " + (success ? "success" : "fail"));
			if (success) {
				PlayerProfile profile = new PlayerProfile();

				profile.id = packet.getHash();			// id
				profile.username = packet.getString();	// username
				profile.location = packet.getPoint();	// location

				// inventory
				profile.inventory = new Inventory();
				int itemCount = packet.getByte();
				for (int i = 0;i < itemCount;i++)
					profile.inventory.add(packet.getItem());

				// stats
				List<Stat> stats = new ArrayList<Stat>(StatList.NUM_STATS);
				for (int i = 0;i < StatList.NUM_STATS;i++)
					stats.add(packet.getStat());

				profile.stats = new StatList(stats);

				// friends
				profile.friends = new FriendList();
				int friendCount = packet.getShort();
				for (int i = 0;i < friendCount;i++)
					profile.friends.add(packet.getFriend());

				profile.registered = packet.getDate();
				profile.lastSession = packet.getDate();

				client.loginSuccess(profile);
				packet.getSession().removeAttribute("pending");

				String welcomeMessage;
				// First login with this account
				if (profile.lastSession.getTime() == 0)
					welcomeMessage = "Welcome to <game>, " + profile.username + ".";
				else {
					welcomeMessage = "Welcome back " + profile.username + ", your last login was ";
					int days = (int) ((System.currentTimeMillis() - profile.lastSession.getTime()) / 86400000);
					switch (days) {
					case 0: {
						welcomeMessage += "today.";
						break;
					}

					case 1: {
						welcomeMessage += "yesterday.";
					}

					default: {
						welcomeMessage += days + " days ago.";
					}
					}
				}

				client.getHUD().addMessage(welcomeMessage, ChatMessages.Type.MESSAGE);
			}
			else {
				client.loginFailed(packet.getString());
			}
		}
		catch (Exception e) {
			client.loginFailed("Unknown error: " + e.getMessage());
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
}

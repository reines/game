package com.game.server.handlers.packet;

import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.codec.Packet;
import com.game.common.codec.PacketBuilder;
import com.game.common.model.Friend;
import com.game.common.model.Hash;
import com.game.common.model.Inventory;
import com.game.common.model.Item;
import com.game.common.model.PlayerProfile;
import com.game.common.model.Stat;
import com.game.common.util.ISAACAlgorithm;
import com.game.common.util.StatList;
import com.game.server.Server;
import com.game.server.WorldManager;
import com.game.server.handlers.PacketHandler;
import com.game.server.model.Player;

public class LoginHandler implements PacketHandler {
	private static final Logger log = LoggerFactory.getLogger(LoginHandler.class);

	@Override
	public void handlePacket(Server server, WorldManager world, Player player, Packet packet) throws Exception {
		IoSession session = packet.getSession();
		player = this.handleLoginRequest(server, world, packet);

		// If the login failed, close the session (gracefully)
		if (player == null) {
			if (log.isDebugEnabled())
				log.debug("Login failed, closing session");
			session.close(false);
		}
		// If it was successful, attach the client
		else {
			session.setAttribute("client", player);
			session.removeAttribute("pending");

			world.addPlayer(player);
		}
	}

	private Player handleLoginRequest(Server server, WorldManager world, Packet packet) {
		IoSession session = packet.getSession();
		PacketBuilder response = new PacketBuilder(Packet.Type.LOGIN_RESPONSE);

		try {
			Hash id = packet.getHash();
			Hash pass = packet.getHash();

			long decryptionSeed = packet.getLong();
			session.setAttribute("decrypter", new ISAACAlgorithm(decryptionSeed));
			long encryptionSeed = packet.getLong();
			session.setAttribute("encrypter", new ISAACAlgorithm(encryptionSeed));

			PlayerProfile profile = server.getDatabase().getPlayerProfile(id, pass);
			// The profile wasn't found (i.e. the user and/or pass was wrong)
			if (profile == null) {
				response.putBoolean(false);
				response.putString("Invalid username and/or password.");

				return null;
			}

			// The client is already logged in
			if (server.getWorldManager().getPlayer(id) != null) {
				response.putBoolean(false);
				response.putString("Account already in use.");

				return null;
			}

			// Confirm the profile isn't corrupt
			if (profile.stats.size() != StatList.NUM_STATS || profile.inventory.size() > Inventory.MAX_SIZE) {
				response.putBoolean(false);
				response.putString("Profile corrupt! Please contact support.");

				log.error("Corrupt profile found for user: " + id);
				return null;
			}

			response.putBoolean(true);

			// Send some user related information so the client knows who they are
			response.putHash(profile.id);				// id
			response.putString(profile.username);		// username
			response.putPoint(profile.location);		// location

			// inventory
			response.putByte((byte) profile.inventory.size());
			for (Item item : profile.inventory)
				response.putItem(item);

			// stats
			for (Stat stat : profile.stats)
				response.putStat(stat);

			// friends
			response.putShort((short) profile.friends.size());
			for (Friend friend : profile.friends)
				response.putFriend(friend);

			response.putDate(profile.registered);
			response.putDate(profile.lastSession);

			return new Player(server, session, profile);
		}
		catch (Exception e) {
			response.putBoolean(false);
			response.putString(e.getMessage());

			e.printStackTrace();

			return null;
		}
		finally {
			session.write(response);
		}
	}
}

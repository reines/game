package com.game.server.model;

import java.util.Collection;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.game.common.codec.Packet;
import com.game.common.codec.PacketBuilder;
import com.game.common.model.Entity;
import com.game.common.model.Friend;
import com.game.common.model.Hash;
import com.game.common.model.Inventory;
import com.game.common.model.Item;
import com.game.common.model.Path;
import com.game.common.model.PlayerProfile;
import com.game.common.model.Point;
import com.game.common.model.Stat;
import com.game.common.util.EntityList;
import com.game.common.util.FriendList;
import com.game.common.util.StatList;
import com.game.server.Server;
import com.game.server.WorldManager;

public class Player extends Entity implements Observer {
	private static final Logger log = LoggerFactory.getLogger(Player.class);

	public static final int VIEW_DISTANCE = 64;

	protected final Server server;
	protected final WorldManager world;
	protected final IoSession session;
	protected final PlayerProfile profile;
	protected final EntityList<Player> knownPlayers;
	protected Path path;

	public Player(Server server, IoSession session, PlayerProfile profile) {
		this.server = server;
		this.session = session;
		this.profile = profile;

		for (Stat stat : profile.stats)
			stat.addObserver(this);

		path = null;
		world = server.getWorldManager();
		knownPlayers = new EntityList<Player>();

		// Once a client is logged in, we can relax the idle time to 60 seconds
		session.getConfig().setIdleTime(IdleStatus.READER_IDLE, 60);
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public void update(long now, EntityList<Player> allPlayers) {
		// If we have a path set, move to the next step along
		if (path != null) {
			Point step = path.removeNext();
			if (!path.hasNext())
				path = null;

			// The next step is next to us and a valid step
			if (world.getMap().isValidStep(profile.location, step)) {
				this.setLocation(step);
			}
			// The next step isn't next to us, we have an invalid path!
			else {
				// TODO: This can happen if the player is walking and they generate a new path, can we someone handle this nicely?
				log.warn("Received invalid path from: " + this);
				path = null;
			}
		}

		// For each player we know about, check they are still within our view area and logged in
		for (Iterator<Player> it = knownPlayers.iterator();it.hasNext();) {
			Player player = it.next();
			double distance = this.getLocation().distanceTo(player.getLocation());
			if (distance > VIEW_DISTANCE || !allPlayers.contains(player))
				it.remove();
		}

		// For each player in our view area, check they are in our known players list
		for (Player player : allPlayers) {
			double distance = this.getLocation().distanceTo(player.getLocation());
			if (distance > VIEW_DISTANCE)
				continue;

			knownPlayers.add(player);
		}

		// Alert this player of new players
		if (knownPlayers.hasNewEntities())
			this.sendAddPlayers(knownPlayers.newEntities());

		// Alert this player of removed players
		if (knownPlayers.hasRemovedEntities())
			this.sendRemovePlayers(knownPlayers.removedEntities());

		// Alert this player of updated players
		if (knownPlayers.hasUpdatedEntities())
			this.sendUpdatePlayers(knownPlayers.updatedEntities());

		// Reset the new and removed list ready for next time
		knownPlayers.reset();
	}

	public Collection<Player> getKnownPlayers() {
		return knownPlayers.allEntities();
	}

	public StatList getStats() {
		return profile.stats;
	}

	public FriendList getFriends() {
		return profile.friends;
	}

	public Inventory getInventory() {
		return profile.inventory;
	}

	public void onSessionStarted() {
		log.info("Client connected: " + this);
		WorldTile tile = world.getMap().getTile(profile.location);
		if (tile == null) {
			log.error("Attempted to add client to non-existant tile: " + this);

			session.close(false);
			return;
		}

		tile.add(this);
	}

	@Override
	public Point getLocation() {
		return profile.location;
	}

	@Override
	public void setLocation(Point p) {
		// If we haven't moved then don't bother updating tiles
		if (profile.location.equals(p))
			return;

		// Remove ourselves from the old tile
		world.getMap().getTile(profile.location).remove(this);

		WorldTile tile = world.getMap().getTile(p);
		if (tile == null) {
			log.error("Attempted to add client to non-existant tile: " + this);

			session.close(false);
			return;
		}

		// Update our location
		profile.location.set(p);

		// Add ourselves to the new tile
		tile.add(this);

		// Mark us as changed
		super.setChanged();
		super.notifyObservers();
	}

	public String getUsername() {
		return profile.username;
	}

	@Override
	public Hash getID() {
		return profile.id;
	}

	public void write(PacketBuilder packet) {
		session.write(packet);
	}

	public void onSessionEnded() {
		// Remove ourselves from the map
		world.getMap().getTile(profile.location).remove(this);

		// Update our last session time and save our profile
		profile.lastSession.setTime(System.currentTimeMillis());
		server.getDatabase().save(profile);

		log.info("Client disconnected: " + this);
	}

	public void sendChat(Player from, String message) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.CHAT_RESPONSE);

		packet.putHash(from.getID());
		packet.putString(message);

		this.write(packet);
	}

	public void sendMessage(String message) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.MESSAGE_RESPONSE);

		packet.putString(message);

		this.write(packet);
	}

	public void sendInventoryAdd(Item item) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.INVENTORY_ADD_RESPONSE);

		packet.putItem(item);

		this.write(packet);
	}

	public void sendInventoryRemove(int index) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.INVENTORY_REMOVE_RESPONSE);

		packet.putByte((byte) index);

		this.write(packet);
	}

	public void sendInventoryUpdate(int index, Item item) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.INVENTORY_UPDATE_RESPONSE);

		packet.putByte((byte) index);
		packet.putLong(item.getAmount());
		packet.putBoolean(item.isEquiped());

		this.write(packet);
	}

	public void sendAddFriend(Friend friend) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.FRIEND_ADD_RESPONSE);

		packet.putFriend(friend);

		this.write(packet);
	}

	public void sendRemoveFriend(Friend friend) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.FRIEND_REMOVE_RESPONSE);

		packet.putHash(friend.getID());

		this.write(packet);
	}

	public void sendFriendMessage(String username, String message) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.FRIEND_MESSAGE_RESPONSE);

		packet.putString(username);
		packet.putString(message);

		this.write(packet);
	}

	public void sendFriendLogin(Hash id, boolean in) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.FRIEND_LOGIN_RESPONSE);

		packet.putHash(id);
		packet.putBoolean(in);

		this.write(packet);
	}

	public void sendAddPlayers(Collection<Player> players) {
		// Remove ourself, we already know where we are!
		int playerCount = players.size();
		if (players.contains(this))
			playerCount--;

		// If there's no players then we have nothing to send
		if (playerCount == 0)
			return;

		PacketBuilder packet = new PacketBuilder(Packet.Type.PLAYERS_ADD_RESPONSE);

		packet.putShort((short) playerCount);
		for (Player player : players) {
			if (player.equals(this))
				continue;

			packet.putHash(player.getID());
			packet.putString(player.getUsername());
			packet.putPoint(player.getLocation());
		}

		this.write(packet);
	}

	public void sendRemovePlayers(Collection<Player> players) {
		// Remove ourself, we already know where we are!
		int playerCount = players.size();
		if (players.contains(this))
			playerCount--;

		// If there's no players then we have nothing to send
		if (playerCount == 0)
			return;

		PacketBuilder packet = new PacketBuilder(Packet.Type.PLAYERS_REMOVE_RESPONSE);

		packet.putShort((short) playerCount);
		for (Player player : players) {
			if (player.equals(this))
				continue;

			packet.putHash(player.getID());
		}

		this.write(packet);
	}

	public void sendUpdatePlayers(Collection<Player> players) {
		int playerCount = players.size();
		// If there's no players then we have nothing to send
		if (playerCount == 0)
			return;

		PacketBuilder packet = new PacketBuilder(Packet.Type.PLAYERS_UPDATE_RESPONSE);

		packet.putShort((short) playerCount);
		for (Player player : players) {
			packet.putHash(player.getID());
			packet.putPoint(player.getLocation());
		}

		this.write(packet);
	}

	public void sendUpdateStat(Stat stat) {
		PacketBuilder packet = new PacketBuilder(Packet.Type.STAT_UPDATE_SEND);

		packet.putStat(stat);

		this.write(packet);
	}

	@Override
	public int hashCode() {
		return profile.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Player))
			return false;

		Player c = (Player) o;
		return this.getID().equals(c.getID());
	}

	@Override
	public String toString() {
		return "player[id = " + this.getID() + ", username = '" + profile.username + "']";
	}

	@Override
	public void update(Observable o, Object arg) {
		// A stat was updated, send the update to the client
		if (o instanceof Stat) {
			Stat stat = (Stat) o;
			boolean levelNotification = (Boolean) arg;
			if (levelNotification)
				this.sendMessage("You increased a " + stat.getType().name + " level!");
			else
				this.sendUpdateStat(stat);
		}
	}
}

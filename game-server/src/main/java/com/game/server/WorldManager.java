package com.game.server;

import java.util.Collection;

import com.game.common.model.Hash;
import com.game.common.util.EntityList;
import com.game.server.model.Player;
import com.game.server.model.WorldMap;

public class WorldManager {
	public static final int PLAYER_UPDATE_DELAY = 200;

	protected final Server server;
	protected final WorldMap map;
	protected final EntityList<Player> players;
	protected long lastPlayerUpdate;

	public WorldManager(Server server) {
		this.server = server;

		map = WorldMap.load();
		players = new EntityList<Player>();

		lastPlayerUpdate = 0;
	}

	public WorldMap getMap() {
		return map;
	}

	public void update(long now) {
		if (now - lastPlayerUpdate > PLAYER_UPDATE_DELAY) {
			lastPlayerUpdate = now;

			synchronized (players) {
				// Update all the players
				for (Player player : players.allEntities())
					player.update(now, players);

				Collection<Player> newPlayers = players.newEntities();
				Collection<Player> removedPlayers = players.removedEntities();

				// Alert all the players of any logins/logouts
				for (Player player : players.allEntities()) {
					// Alert this player if any of the new players are their friends
					for (Player newPlayer : newPlayers) {
						if (player.getFriends().contains(newPlayer.getID()))
							player.sendFriendLogin(newPlayer.getID(), true);
					}

					// Alert this player is any of the removed players are their friends
					for (Player removedPlayer : removedPlayers) {
						if (player.getFriends().contains(removedPlayer.getID()))
							player.sendFriendLogin(removedPlayer.getID(), false);
					}
				}

				players.reset();
			}
		}
	}

	public Collection<Player> getPlayers() {
		return players.allEntities();
	}

	public Player getPlayer(Hash id) {
		return players.get(id);
	}

	public void addPlayer(Player player) {
		synchronized (players) {
			player.onSessionStarted();
			players.add(player);
		}
	}

	public void removePlayer(Player player) {
		synchronized (players) {
			players.remove(player.getID());
			player.onSessionEnded();
		}
	}
}

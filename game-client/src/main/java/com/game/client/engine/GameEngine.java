package com.game.client.engine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.game.client.Client;
import com.game.client.model.LocalMap;
import com.game.client.model.Player;
import com.game.common.model.Hash;
import com.game.common.model.Path;
import com.game.common.model.Point;
import com.game.graphics.models.Model;
import com.game.graphics.renderer.Graphics3D;
import com.game.graphics.renderer.Pickable;

public abstract class GameEngine {

	protected final Client client;
	protected final LocalMap map;
	protected final Camera camera;
	protected final Map<Hash, Player> players;
	protected Player self;

	public GameEngine(Client client) {
		this.client = client;

		map = LocalMap.load(client.getGraphics());
		camera = new Camera(client.getKeyboard());

		players = new HashMap<Hash, Player>();

		self = null;
	}

	public Camera getCamera() {
		return camera;
	}

	public LocalMap getMap() {
		return map;
	}

	public Collection<Player> getPlayers() {
		return players.values();
	}

	public Player getPlayer(Hash id) {
		return players.get(id);
	}

	public void addPlayer(Player player) {
		players.put(player.getID(), player);
	}

	public Player removePlayer(Hash id) {
		return players.remove(id);
	}

	public void init(Player self) {
		this.self = self;
		// Add ourself to the player list
		this.addPlayer(self);

		// Load the appropriate map sectors
		map.setLocation(self.getLocation());

		// Reset the camera rotation
		camera.setRotation(Camera.ROTATION_DEFAULT);
	}

	public void update(long now) {
		// Update the camera
		camera.update(now);
	}

	public final void display(Graphics3D g) {
		// Draw our terrain
		map.draw();

		// Draw all players
		for (Player player : players.values()) {
			Point location = player.getLocation();
			g.drawModel(location.x, location.y, player.getModel());
		}

		// TODO: Draw the click mark
	}

	public final void mouseClicked(int x, int y, boolean left) {
		Graphics3D g = client.getGraphics().get3D();

		g.beginPicking();
		{
			this.display(g);
		}
		Pickable item = g.endPicking(x, y);
		if (item == null) // Nothing was clicked on
			return;

		// The ground was clicked
		if (map.isTileBuffer(item)) {
			// Calculate where was clicked
			Point target = g.translateCoordinates(x, y);
			if (target == null)
				return;

			// Calculate a path from here to where was clicked
			Path path = map.generatePath(self.getLocation(), target);
			if (path == null)
				return;

			// Left click means we want to walk there
			if (left) {
				// TODO: Draw a mark where we clicked

				// Send walk command
				client.getWorldManager().sendWalkTo(path);
			}
		}
		// The model was clicked
		else if (item instanceof Model) {
			// If we clicked ourself, ignore it
			if (item != self.getModel())
				this.handleModelClicked(item, x, y, left);
		}
	}

	private void handleModelClicked(Pickable item, int x, int y, boolean left) {
		for (Player player : players.values()) {
			if (item == player.getModel()) {
				// Left click means action - attack/walk to
				if (left) {
					// TODO: Check if we mean attack or walk to - send command
					System.out.println("Attack/Walk-to: " + player);

					// TODO: Draw a mark where we clicked
				}
				else {
					// TODO: Menu with attack/trade/follow/etc. options
					System.out.println("Show menu for: " + player);
				}

				return;
			}
		}

		// An unknown model was clicked, ignore it
	}

	public Point getLocation() {
		return self.getLocation();
	}

	public void setLocation(Point location) {
		self.setLocation(location);
		map.setLocation(location);
	}
}
